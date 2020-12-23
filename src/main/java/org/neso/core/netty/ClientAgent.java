package org.neso.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.neso.core.exception.ClientAbortException;
import org.neso.core.request.handler.RequestHandler;
import org.neso.core.server.ServerContext;
import org.neso.core.server.ServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * request 처리 스레드에 의해서 공유된다.
 * 
 */
public class ClientAgent extends SessionClientImpl {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	final private ByteLengthBasedReader reader;
	
	final private Bbw writer;
	
	final private ReentrantLock writeLock = new ReentrantLock();
	
	final private boolean isActiveWriteLock;	//락이 필요 없는 경우 오버헤드를 줄이기 위해 

    private boolean writable = true;	//락 획득 이후에 참조 됨 - 메모리 동기화 필요없음
    
    
    final private ServerContext serverContext;
    
    
	public ClientAgent(SocketChannel socketChannel, ServerContext context) {
		super(socketChannel);
		
		
		this.serverContext = context;
		
		ServerOptions options = context.options();

		this.isActiveWriteLock = !context.requestExecutor().isRunOnIoThread();

    	this.reader = new ByteLengthBasedHeadBodyReader(this, serverContext.requestHandler(), serverContext.requestFactory(), 
    			options.isInoutLogging(), options.isConnectionOriented(), options.getMaxRequestBodyLength(), options.getReadTimeoutMillis());
    	
    	this.writer = new Bbw(serverContext.requestHandler(), options.isInoutLogging(), options.getWriteTimeoutMillis(), options.isConnectionOriented());
	}
	
    @Override
    public ServerContext getServerContext() {
        return this.serverContext;
    }
	
	public ByteLengthBasedReader getReader() {
		return this.reader;
	}
    
    @Override
    public boolean isConnected() {
    	return socketChannel().isOpen();
    }
	
    @Override
    public void disconnect() {
    	if (!isActiveWriteLock) {
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
    	if (!isActiveWriteLock) {
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
    	if (!isActiveWriteLock) {
    		return writer;
    	}
    	
    	if (writeLock.isHeldByCurrentThread()) {
    		return writer;
    	} else {
    		writeLock.lock();
        	return writer;
    	}
    }
    
    
    
    
    public class Bbw implements ByteBasedWriter {
    	
    	private ChannelFuture lastCf = socketChannel().newSucceededFuture();
    	
        final private boolean inoutLogging;
        
        final private RequestHandler requestHandler;
        
        final private int writeTimeoutMillis;
        
        final private boolean connectionOriented;
        
        public Bbw(RequestHandler requestHandler, boolean inoutLogging, int writeTimeoutMillis, boolean connectionOriented) {
        	this.requestHandler = requestHandler;
			this.inoutLogging = inoutLogging;
			this.writeTimeoutMillis = writeTimeoutMillis;
			this.connectionOriented = connectionOriented;
		}
        
    	public ChannelFuture getCh() {
    		return lastCf;
    	}
    	
    	@Override
    	public void write(byte b) {
    		write(new byte[]{b});
    	}
    	
		@Override
		public void write(byte[] bytes) {
			ByteBuf buf =  socketChannel().alloc().buffer(bytes.length);
			buf.writeBytes(bytes);
			write(buf);
		}

		@Override
		public void write(final ByteBuf buf) {
			
			if (writable) {

				if (isConnected()) {
					
					if (inoutLogging) {
						logger.info(BufUtils.bufToString("WRITE RESPONSE ", buf));	//TODO 리스너안으로 로그를 넣어야 하는데.. 과연 BUF복사 비용까지.. 들이면서 해야하나..
					}
					
					final ChannelFuture cf = lastCf.channel().write(buf);
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
				
			} else {
				//logger.debug("쓰기 불가로 인해 쓰기 작업 중단");
			}
		}


		@Override
		public void close() {
			
			socketChannel().flush();
			
			if (!connectionOriented) {
				if (writable) {
					disconnect();
					writable = false;
				}
			}
			
			if (writeLock.isHeldByCurrentThread()) {
				//logger.debug("writer lock release");
				writeLock.unlock();
			}
		}
    }


}