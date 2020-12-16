package org.neso.core.netty;

import java.util.concurrent.TimeUnit;

import org.neso.core.exception.OverReadBytesException;
import org.neso.core.netty.ByteLengthBasedReader.ReaderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


/**
 * byte array 길이 기반으로 데이터를 읽는 ChannelInboundHandlerAdapter
 * 
 * @see ByteLengthBasedReader
 * 
 */
public final class ByteLengthBasedInboundHandler extends ChannelInboundHandlerAdapter {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    final private ByteLengthBasedReader reader;
    
    final private int readTimeoutMillisOnRead;
	
    private ByteBuf toReadBuf;
    
    public ByteLengthBasedInboundHandler(ByteLengthBasedReader reader) {
    	this(reader, -1);
	}
    
    public ByteLengthBasedInboundHandler(ByteLengthBasedReader reader,  int readTimeoutMillisOnRead) {
    	this.reader = reader;
    	this.readTimeoutMillisOnRead = readTimeoutMillisOnRead;
	}
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	reader.init();
    	
    	int toReadBytes = reader.getToReadBytes();
    	if (toReadBytes < 1) {
    		throw new RuntimeException("cant read ...");
    	}
    	toReadBuf = ctx.alloc().buffer(toReadBytes);
    	
    	if (reader.getStatus() == ReaderStatus.ING) {
    		addReadTimeoutHandler(ctx);
    	}
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	
    	try {
    		if (msg instanceof ByteBuf) {
    			ByteBuf inputBuf = (ByteBuf) msg;
		        
				while (inputBuf.isReadable()) {

		    		if (reader.getStatus() == ReaderStatus.CLOSE) {	//데이터는 더 들어왔는데... 읽기가 종료되었다..
			    		
			    		byte[] overBytes = BufUtils.copyToByteArray(inputBuf);
	    		    	throw new OverReadBytesException(overBytes);
	    		    	
		    		} else {
			    		addReadTimeoutHandler(ctx);
			    		
			    		int toReadlength = Math.min(toReadBuf.writableBytes(), inputBuf.readableBytes());
	    	        	inputBuf.readBytes(toReadBuf, toReadlength);
	    	        	 
	    	        	if (!toReadBuf.isWritable()) {	//읽어야 할 바이트를 다 읽었다면..
	    	        		reader.onRead(toReadBuf.copy());
	    	        		
	    	        		if (reader.getStatus() != ReaderStatus.ING) {
	    	        			removeReadTimeoutHandler(ctx);
	    	        		}
	    	        		
	        	        	int toReadBytes = reader.getToReadBytes();
	                        toReadBuf.clear();
	                    	toReadBuf.capacity(toReadBytes);
	    	        	}
		    		}
		    	}
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
    }
    
    private void addReadTimeoutHandler(ChannelHandlerContext ctx) {
    	if (readTimeoutMillisOnRead > 0 && ctx.channel().pipeline().get(AsyncCloseReadTimeoutHandler.class.getSimpleName()) == null) {
    		ctx.channel().pipeline().addBefore(ByteLengthBasedInboundHandler.class.getSimpleName(), AsyncCloseReadTimeoutHandler.class.getSimpleName(), new AsyncCloseReadTimeoutHandler(readTimeoutMillisOnRead, TimeUnit.MILLISECONDS, reader));
		}
    }
    
    private void removeReadTimeoutHandler(ChannelHandlerContext ctx) {
    	if (readTimeoutMillisOnRead > 0 && ctx.channel().pipeline().get(AsyncCloseReadTimeoutHandler.class.getSimpleName()) != null) {
			ctx.channel().pipeline().remove(AsyncCloseReadTimeoutHandler.class.getSimpleName());
		}
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	if (toReadBuf != null && toReadBuf.refCnt() > 0) {
    		ReferenceCountUtil.release(toReadBuf);
    	}
    	reader.destroy();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {	
    	reader.onReadException(cause);
    }
}