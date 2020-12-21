package org.neso.core.netty;

import static io.netty.util.internal.StringUtil.NEWLINE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * request 처리 스레드에 의해서 공유된다.
 * 
 */
public class ClientAgent extends SessionClientImpl {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	final private ReentrantLock writeLock = new ReentrantLock();
	
	final private boolean needWriteLock;	//락이 필요 없는 경우 오버헤드를 줄이기 위해 
	
	final private ByteLengthBasedReader reader;
	
	final private Bbw writer;

    private boolean writable = true;
    
    
    final private ServerContext serverContext;
    
    //severContext copy value .. serverContext 의존성을 제거할까 고민.. session에서 접근 권한을 주는게 이득인가...
    final private boolean inoutLogging;
    
    final private RequestHandler requestHandler;
    
    final private int writeTimeoutMillis;
    
    final private boolean connectionOriented;
    
   
    
	public ClientAgent(SocketChannel sc, ServerContext serverContext) {
		super(sc);
		this.serverContext = serverContext;
		this.inoutLogging = serverContext.options().isInoutLogging();
		this.requestHandler = serverContext.requestHandler();
		this.writeTimeoutMillis = serverContext.options().getWriteTimeoutMillis();
		this.connectionOriented = serverContext.options().isConnectionOriented();
		
		this.needWriteLock = !serverContext.requestExecutor().isRunOnIoThread();
    	this.reader = new HeadBodyRequestReader(this, serverContext);
    	this.writer = new Bbw();
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
	

    private void log(ByteBuf readedBuf) {
    	String eventName = "RESPONSE WRITE";
		int length = readedBuf.readableBytes();
		int offset = readedBuf.readerIndex();
        int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
        StringBuilder dump = new StringBuilder(eventName.length() + 2 + 10 + 1 + 2 + rows * 80);

        dump.append(eventName).append(": ").append(length).append('B').append(NEWLINE);
    	ByteBufUtil.appendPrettyHexDump(dump, readedBuf, offset, length);

    	readedBuf.resetReaderIndex();
    	
    	logger.info(dump.toString());
    }
    

    
    @Override
    public void disconnect() {
    	if (!needWriteLock) {
    		disconnectProc();
    		return;
    	}
    	if (writeLock.isHeldByCurrentThread()) {
    		disconnectProc();
    		
    	} else {
    		
    		writeLock.lock();
    		//logger.debug("disconnect lock get");
    		try {
    			disconnectProc();
    			
			} finally {
    			//logger.debug("disconnect lock release!!!!!!!!!!");
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
				} else {
					//logger.debug("이미 접속 종료로 인해 무시!!");
				}
			}
		});
    }
    
    @Override
    public void releaseWriterLock() {
    	if (!needWriteLock) {
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
    	if (!needWriteLock) {
    		return writer;
    	}
    	
    	if (writeLock.isHeldByCurrentThread()) {
    		return writer;
    	} else {
    		writeLock.lock();
    		//logger.debug("writer lock get");
        	return writer;
    	}
    }
    
    
    public class Bbw implements ByteBasedWriter {
    	
    	private ChannelFuture lastCf = socketChannel().newSucceededFuture();
    	
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
						log(buf);	//TODO 리스너안으로 로그를 넣어야 하는데.. 과연 BUF복사 비용까지.. 들이면서 해야하나..
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


	@Override
	public String toString() {
		return socketChannel().toString();
	}
}