package org.neso.core.request.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.neso.core.request.Client;
import org.neso.core.request.Session;
import org.neso.core.server.internal.BufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

/**
 * 메모리 기반 request
 * 
 */
public class InMemoryHeadBodyRequest implements OperableHeadBodyRequest {
    
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final private long requestTime;
	
    private byte[] allBytes = new byte[0];
    
    private Map<String, Object> attrMap = new LinkedHashMap<String, Object>();
    
    private boolean readHead = false;
    
    private boolean readBody = false;
    
    private byte[] headBytes = null;
    
    private byte[] bodyBytes = null;
    
    private ByteBuf bodyByteBuf;
    
    private Client client;
    
    public InMemoryHeadBodyRequest(Client client) {
    	this.requestTime = System.currentTimeMillis();
	}
    
    @Override
    public Session getSession() {
    	return client;
    }
    
	@Override
	public long getRequestTime() {
		return requestTime;
	}

    @Override
    public void addAttribute(String key, Object value) {
    	attrMap.put(key, value);
    }

	@Override
	public boolean removeAttribute(String key) {
		if (attrMap.containsKey(key)) {
        	return attrMap.remove(key) != null ? true : false;
    	} else {
    		return false;
    	}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public <T> T getAttribute(String key) {
    	if (attrMap.containsKey(key)) {
        	return (T) attrMap.get(key);
    	} else {
    		return null;
    	}
    }
	
    @Override
    public byte[] getHeadBytes() {
    	return headBytes;
    }
    
    public byte[] getBodyBytes() {
    	if (isReadedBody()) {
    		if (bodyBytes == null) {
    			if (bodyByteBuf.refCnt() > 0) {
    				if (bodyByteBuf.readerIndex() > 0) {
    					bodyByteBuf.resetReaderIndex();
    				}
    				
    				this.bodyBytes = BufUtils.copyToByteArray(bodyByteBuf);
        			ReferenceCountUtil.release(bodyByteBuf);
        			this.allBytes = ArrayUtils.addAll(headBytes, bodyBytes);
        			return bodyBytes;
    			} else {
    				throw new RuntimeException("cant read buf..");
    			}
    			
    		} else {
    			return bodyBytes;
    		}
    	}
    	return null;
    }
    

	@Override
	public byte[] getAllBytes() {
		if (isReadedBody()) {
			//buf -> byte array로 아직 안 옮겨졌을 수도 있으니..
			getBodyBytes();
		}
		return this.allBytes;
	}

	@Override
	public void setHeadBytes(ByteBuf readedBuf) {
		this.headBytes = BufUtils.copyToByteArray(readedBuf);
		ReferenceCountUtil.release(readedBuf);
		this.allBytes = this.headBytes;
		this.readHead = true;
	}
	
	@Override
	public boolean isReadedHead() {
		return this.readHead;
	}
	
	@Override
	public void setBodyBytes(ByteBuf readedBuf) {
		this.bodyByteBuf = readedBuf;
		this.readBody =true;
	}
	
	@Override
	public boolean isReadedBody() {
		return this.readBody;
	}


	@Override
	public ByteBuf getDirectBodyBuf() {
		if (isReadedBody()) {
			if (bodyBytes == null) { //byte array로 옮기지 않았다면..
				 return bodyByteBuf;
			} else {
				//이미 byte array로 옮겨졌다면 예외 발생
				throw new RuntimeException("Already body buf read!..");
			}
		} else {
			throw new RuntimeException("Not read body..");
		}
	}

	
	@Override
	public void release() {
		if (bodyByteBuf != null ) {
			if (bodyBytes == null) {//buf에서 array로 안 옮겨졌다면..
				if (bodyByteBuf.refCnt() > 0) {
					ReferenceCountUtil.release(bodyByteBuf);
				}
			}
			logger.debug("request buf ..released .... {} !!", bodyByteBuf.refCnt() == 0 ? "ok" : "fail");
		}
	}




}
