package org.neso.core.netty;

import static io.netty.util.internal.StringUtil.NEWLINE;


import org.neso.core.request.Client;
import org.neso.core.request.factory.RequestFactory;
import org.neso.core.request.handler.RequestHandler;
import org.neso.core.request.internal.OperableHeadBodyRequest;
import org.neso.core.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class HeadBodyRequestReader implements ByteLengthBasedReader {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	 
	final private Client client;
	
	
	private OperableHeadBodyRequest currentRequest;
	
	private ReaderStatus readStatus = null;

	
	//ServerContext copy
	final private boolean inoutLogging;
	
	final private RequestHandler requestHandler;
	
	final private RequestFactory requestFactory;
    
    final private boolean connectionOriented;
    
    final private int maxRequestBodyLength;
    
    
	public HeadBodyRequestReader(Client client, ServerContext serverContext) {
		this.client = client;
		this.inoutLogging = serverContext.options().isInoutLogging();
		this.requestHandler = serverContext.requestHandler();
		this.requestFactory = serverContext.requestFactory();
		this.connectionOriented = serverContext.options().isConnectionOriented();
		this.maxRequestBodyLength = serverContext.options().getMaxRequestBodyLength();
	}
	
	@Override
	public void init() {
		this.currentRequest = requestFactory.newHeadBodyRequest(client);
		requestHandler.onConnect(client);
		readStatus = connectionOriented? ReaderStatus.STANBY: ReaderStatus.ING;
	}

	@Override
	public void destroy() {
		requestHandler.onDisconnect(client);
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
    		int headLength = requestHandler.headLength();
    		if (headLength < 1) {
    			throw new RuntimeException("Header length cannot be zero or a negative number ");
    		}
    		return headLength;
    		
		} else { //if (!currentRequest.isReadedBody()) 
			
			int bodyLength = requestHandler.bodyLength(currentRequest);
			
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
				log("HEADER RECEIVED", readedBuf);
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
				log("BODY RECEIVED", readedBuf);
			}
			
			if (getToReadBytes() != readedBuf.capacity()) {
				throw new RuntimeException("it's different expected bytes");
			}
			
			currentRequest.setBodyBytes(readedBuf);
			
			requestHandler.onRequest(client, currentRequest);
    		
    		if (connectionOriented) {	//요청을 계속 받을 수 있다면. 새 리퀘스트를 만들어 놓고..
    			this.currentRequest = requestFactory.newHeadBodyRequest(client);
    			readStatus = ReaderStatus.STANBY;
			} else {
				readStatus = ReaderStatus.CLOSE;
			}
		}
    }
    
    private void log(String eventName, ByteBuf readedBuf) {
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
	public void onReadException(Throwable th) {

    	try {
    		requestHandler.onExceptionRead(client, th);
    	}catch (Exception e) {
			logger.error("occurred exception.. requestHandler's onExceptionRead", e);
		}
	}
}
