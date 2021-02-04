package org.neso.core.request.internal;


import org.neso.core.request.HeadBodyRequest;

import io.netty.buffer.ByteBuf;

/**
 * 내부 인터페이스
 * 
 * request 조작 가능한 메소드 분리
 */
public interface OperableHeadBodyRequest extends HeadBodyRequest {
	
	public void setHeadBytes(ByteBuf readedBuf);
	
	public void setBodyBytes(ByteBuf readedBuf);
	
	public boolean isReadedHead();

	public boolean isReadedBody();
	
	public void release();
	
}

