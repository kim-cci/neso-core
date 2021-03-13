package org.neso.core.server.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.neso.core.exception.ClientAbortException;
import org.neso.core.request.Client;
import org.neso.core.request.factory.RequestFactory;
import org.neso.core.request.handler.RequestHandler;
import org.neso.core.request.internal.OperableHeadBodyRequest;
import org.neso.core.server.ServerContext;
import org.neso.core.server.request.task.RequestTask;
import org.neso.core.support.RequestRejectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Client구현체
 * 
 * 쓰기&접속종료 작업 시 스레드 간 경합 발생
 * getWriter() 로 writer 선점하여 락 획득 후 쓰기 작업 가능
 * 
 */
public class ClientAgent extends SessionClientImpl {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	final private Bbr reader;
	
	final private Bbw writer;
	
	final private ReentrantLock writeLock;

    private boolean writable = true;	//공유 변수 : 락 획득 이후에 참조되기 때문에 메모리 동기화 필요없음
    
    final private ServerContext serverContext;
    
    final private boolean inoutLogging;
    final private boolean connectionOriented;
    final private RequestHandler requestHandler;
    
    
	public ClientAgent(SocketChannel socketChannel, ServerContext context) {
		super(socketChannel);
		this.serverContext = context;
		
		this.inoutLogging = context.options().isInoutLogging();
		this.connectionOriented = context.options().isConnectionOriented();
		this.requestHandler = serverContext.requestHandler();
		this.writeLock = context.requestExecutor().isRunOnIoThread()? null : new ReentrantLock();		//락이 필요 없는 경우 오버헤드를 줄이기 위해 

    	this.reader = new Bbr(serverContext.requestFactory() , serverContext.options().getMaxRequestBodyLength(), serverContext.options().getReadTimeoutMillis());
    	this.writer = new Bbw(serverContext.options().getWriteTimeoutMillis());
	}
	
    @Override
    public ServerContext getServerContext() {
        return this.serverContext;
    }
	
	public ByteLengthBasedReader getReader() {
		return this.reader;
	}

	
    @Override
    public void disconnect() {
    	if (writeLock == null) {
    		disconnectProc();
    		return;
    	}
    	
    	if (writeLock.isHeldByCurrentThread()) {
    		disconnectProc();
    		
    	} else {
    		
    		writeLock.lock();
    		try {
    			disconnectProc();
    			
			} finally {
				writeLock.unlock();
			}
    	}
    }
    
    private void disconnectProc() {
		final ChannelFuture cf = writer.getCh();
		cf.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture lastResult) throws Exception {
				if (cf.channel().isOpen()) {
					cf.channel().close().addListener(new ChannelFutureListener() {
						
						@Override
						public void operationComplete(ChannelFuture lastResult) throws Exception {
							//logger.debug("disconnected!!");
						}
					});
				}
			}
		});
    }
    
    @Override
    public void releaseWriterLock() {
    	if (writeLock == null) {
    		getWriter().close();
    		return;
    	}
    	
    	if (writeLock.isHeldByCurrentThread()) {
    		//logger.debug("client release ... unlock");
    		getWriter().close();
    	}
    }
    
    @Override
    public ByteBasedWriter getWriter() {
    	if (writeLock == null) {
    		return writer;
    	}
    	
    	if (writeLock.isHeldByCurrentThread()) {
    		return writer;
    	} else {
    		writeLock.lock();
        	return writer;
    	}
    }
    
    
    public class Bbr implements ByteLengthBasedReader {
    	
    	final Logger logger = LoggerFactory.getLogger(this.getClass());
 
    	
    	private OperableHeadBodyRequest currentRequest;
    	
    	private ReaderStatus readStatus = null;
    	
    	final private RequestFactory requestFactory;
        
        final private int maxRequestBodyLength;
        
    	final private int readTimeoutMillis;
        
    	public Bbr(RequestFactory requestFactory, int maxRequestBodyLength, int readTimeoutMillis) {
    	
    		this.requestFactory = requestFactory;
    		this.maxRequestBodyLength = maxRequestBodyLength;
    		this.readTimeoutMillis = readTimeoutMillis;
    	}
    	
    	@Override
    	public int getReadTimeoutMillis() {
    		return this.readTimeoutMillis;
    	}
    	
    	@Override
    	public void init() {
    		this.currentRequest = requestFactory.newHeadBodyRequest(ClientAgent.this);
    		requestHandler.onConnect(ClientAgent.this);
    		readStatus = connectionOriented? ReaderStatus.STANBY: ReaderStatus.ING;
    	}

    	@Override
    	public void destroy() {
    		requestHandler.onDisconnect(ClientAgent.this);
    	}
        
    	@Override
    	public ReaderStatus getStatus() {
    		return readStatus;
    	}
    	
        @Override
        public int getToReadBytes() {
        	if (ReaderStatus.CLOSE == readStatus) {
        		return 0; //throw new RuntimeException("reader is closed...");
        	}
        	
        	if (!currentRequest.isReadedHead()) {
        		int headLength = 0;
        		
        		try {
        			headLength = requestHandler.headLength();
        		} catch (Exception e) {
        			throw new RuntimeException("invalid head length", e);
				}
        		
        		if (headLength < 1) {
        			throw new RuntimeException("Header length cannot be zero or a negative number ");
        		}
        		return headLength;
        		
    		} else { //if (!currentRequest.isReadedBody()) 
    			
    			int bodyLength = 0;
    			try {
    				bodyLength = requestHandler.bodyLength(currentRequest);
    			} catch (Exception e) {
					throw new RuntimeException("invalid body length", e);
				}
    			
    			if (maxRequestBodyLength > 0) {
    				if (maxRequestBodyLength < bodyLength) {
    					throw new RuntimeException("Too long body length..");
    				}
    			}
    					
    			return bodyLength;
    		}
        }

        @Override
        public void onRead(ByteBuf readedBuf) throws Exception {
        	
        	if (ReaderStatus.CLOSE == readStatus) {
        		throw new RuntimeException("reader is closed...");
        	}
        	
        	
    		if (!currentRequest.isReadedHead()) {
    			
    			if (inoutLogging) {
    				logger.info(BufUtils.bufToString("RECEIVED REQUEST HEAD", readedBuf));
    			}
    			
    			if (getToReadBytes() != readedBuf.capacity()) {
    				throw new RuntimeException("it's different expected bytes");
    			}
    			
    			currentRequest.setHeadBytes(readedBuf);
    			
    			if (getToReadBytes() == 0) {
                    /**
                     * 바디가 0인 경우, 더 읽어야 할 필요가 없다면.. request 
                     **/
    				onRead(Unpooled.directBuffer(0));
    			}
    			
    			readStatus = ReaderStatus.ING;
    		} else { //if (!currentRequest.isReadedBody()) 
    			
    			if (inoutLogging) {
    				logger.info(BufUtils.bufToString("RECEIVED REQUEST BODY", readedBuf));
    			}
    			
    			if (getToReadBytes() != readedBuf.capacity()) {
    				throw new RuntimeException("it's different expected bytes");
    			}
    			
    			currentRequest.setBodyBytes(readedBuf);
    			
    			completeReadRequest(ClientAgent.this, currentRequest);
        		
        		if (connectionOriented) {	//요청을 계속 받을 수 있다면. 새 리퀘스트를 만들어 놓고..
        			this.currentRequest = requestFactory.newHeadBodyRequest(ClientAgent.this);
        			readStatus = ReaderStatus.STANBY;
    			} else {
    				readStatus = ReaderStatus.CLOSE;
    			}
    		}
        }
        
        
    	@Override
    	public void onReadException(Throwable th) {

        	try {
        		requestHandler.onExceptionRead(ClientAgent.this, th);
        	}catch (Exception e) {
    			logger.error("occurred exception.. requestHandler's onExceptionRead", e);
    		}
    	}
    }
    

    
    private void completeReadRequest(Client client, OperableHeadBodyRequest request) {

		RequestTask task = new RequestTask(client, request);
		
		ServerContext serverContext = client.getServerContext();
		
		if (!serverContext.requestExecutor().registerTask(task)) {
			logger.debug("request cant registered in the request pool, {}", client.toString());
			
			byte[] rejectMessage = "server is too busy".getBytes();
			if (serverContext.requestHandler() instanceof RequestRejectListener) {
				
				RequestRejectListener listener = (RequestRejectListener) serverContext.requestHandler();
			
				try {
					rejectMessage = listener.onRequestReject(serverContext, serverContext.options().getMaxRequests(), request);
				} catch (Exception e) {
					logger.error("occurred requestRejectListner's onRequestReject", e);
				}
			}
			
			ByteBasedWriter writer = client.getWriter();
			writer.write(rejectMessage);
			writer.close();
		} else {
			//logger.debug("request is registered in the request pool");
		}
    }
    
    
    public class Bbw implements ByteBasedWriter {
    	
    	private ChannelFuture lastCf = socketChannel().newSucceededFuture();
    	
    	private boolean didWrite = false;
        
        final private int writeTimeoutMillis;
        
        
        public Bbw(int writeTimeoutMillis) {
			this.writeTimeoutMillis = writeTimeoutMillis;
		}
        
    	public ChannelFuture getCh() {
    		return lastCf;
    	}
    	
    	@Override
    	public ByteBasedWriter write(byte b) {
    		return write(new byte[]{b});
    		
    		
    	}
    	
		@Override
		public ByteBasedWriter write(byte[] bytes) {
			ByteBuf buf =  socketChannel().alloc().buffer(bytes.length);
			buf.writeBytes(bytes);
			return write(buf);
		}

		@Override
		public ByteBasedWriter write(final ByteBuf buf) {
			if (!writable) {
				//
				return this;
			}
			
			
			if (isConnected()) {
				
				if (inoutLogging) {
					logger.info(BufUtils.bufToString("WRITE RESPONSE ", buf));	//TODO 리스너안으로 로그를 넣어야 하는데.. 과연 BUF복사 비용까지.. 들이면서 해야하나..
				}
				
				final ChannelFuture cf = lastCf.channel().write(buf);
				didWrite = true;
				final ScheduledFuture<?> writeTimeoutFuture = socketChannel().eventLoop().schedule(new Runnable() {
					
					@Override
					public void run() {
						if (!cf.isDone()) {
							if (writable) {
								requestHandler.onExceptionWrite(ClientAgent.this, WriteTimeoutException.INSTANCE);
								writable = false;
							}
						}
					}
				}, writeTimeoutMillis, TimeUnit.MILLISECONDS);

				lastCf = cf.addListener(new ChannelFutureListener() {
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						writeTimeoutFuture.cancel(false);
					}
				});
				
    		} else {
    			//logger.debug("접속 종료로 인해...쓰기 작업 중단");
				if (writable) {
					requestHandler.onExceptionWrite(ClientAgent.this, new ClientAbortException(ClientAgent.this));
					writable = false;
				}
    		}
			
			return this;
		}


		@Override
		public void close() {
			
			socketChannel().flush();
			
			if (!connectionOriented && didWrite) {
				if (writable) {
					disconnect();
					writable = false;
				}
			}
			
			if (writeLock != null && writeLock.isHeldByCurrentThread()) {
				//logger.debug("writer lock release");
				writeLock.unlock();
			}
		}
    }


}