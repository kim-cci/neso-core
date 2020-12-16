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
import org.neso.core.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * request 처리 스레드에 의해서 공유된다.
 * 
 */
public class ClientAgent extends SessionImplClient {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	final private ReentrantLock lock = new ReentrantLock();
	
	final private ByteLengthBasedReader headBodyRequestReader;
	
	final private Bbw writer;

    private boolean writable = true;
    
 
	public ClientAgent(SocketChannel sc, ServerContext serverContext) {
		super(sc, serverContext);
    	
    	if (serverContext.options().getWriteTimeoutMillis() < 0) {
    		throw new RuntimeException("writeTimeoutMillis is bigger than zero");
    	}
    
    	this.headBodyRequestReader = new HeadBodyRequestReader(this, getServerContext());
    	this.writer = new Bbw();
	}
	
	
	public ByteLengthBasedReader getReader() {
		return this.headBodyRequestReader;
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

    	if (lock.isHeldByCurrentThread()) {
    		disconnectProc();
    		
    	} else {
    		
    		lock.lock();
    		//logger.debug("disconnect lock get");
    		try {
    			disconnectProc();
    			
			} finally {
    			//logger.debug("disconnect lock release!!!!!!!!!!");
    			lock.unlock();
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
    	if (lock.isHeldByCurrentThread()) {
    		//logger.debug("client release ... unlock");
    		getWriter().close();
    	}
    }
    
    @Override
    public ByteBasedWriter getWriter() {
    	if (lock.isHeldByCurrentThread()) {
    		return writer;
    	} else {
    		lock.lock();
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
					
					if (getServerContext().options().isInoutLogging()) {
						log(buf);	//TODO 리스너안으로 로그를 넣어야 하는데.. 과연 BUF복사 비용까지.. 들이면서 해야하나..
					}
					
					final ChannelFuture cf = lastCf.channel().write(buf);
					final ScheduledFuture<?> writeTimeoutFu = socketChannel().eventLoop().schedule(new Runnable() {
						
						@Override
						public void run() {
							if (!cf.isDone()) {
								if (writable) {
									getServerContext().requestHandler().onExceptionWrite(ClientAgent.this, WriteTimeoutException.INSTANCE);
									writable = false;
								}
							}
						}
					}, getServerContext().options().getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);

					lastCf = cf.addListener(new ChannelFutureListener() {
						
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							writeTimeoutFu.cancel(false);
						}
					});
					
	    		} else {
	    			//logger.debug("접속 종료로 인해...쓰기 작업 중단");
					if (writable) {
						getServerContext().requestHandler().onExceptionWrite(ClientAgent.this, new ClientAbortException(ClientAgent.this));
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
			
			if (!getServerContext().options().isConnectionOriented()) {
				if (writable) {
					disconnect();
					writable = false;
				}
			}
			
			if (lock.isHeldByCurrentThread()) {
				//logger.debug("writer lock release");
				lock.unlock();
			}
		}
    }


	@Override
	public String toString() {
		return socketChannel().toString();
	}
}