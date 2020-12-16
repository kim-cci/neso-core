package org.neso.core.request.internal;


import org.neso.core.request.HeadBodyRequest;

import io.netty.buffer.ByteBuf;

public interface OperableHeadBodyRequest extends HeadBodyRequest {
	
	public void setHeadBytes(ByteBuf readedBuf);
	
	public void setBodyBytes(ByteBuf readedBuf);
	
	public boolean isReadedHead();

	public boolean isReadedBody();
	
	public void release();
	
}

