package org.neso.core.request;

import io.netty.buffer.ByteBuf;

public interface HeadBodyRequest extends HeadRequest {

	/**
	 * @return body byte array
	 */
	public byte[] getBodyBytes(); 
	
	/**
	 * @return head + body byte array
	 */
	public byte[] getAllBytes();
	
	
	public ByteBuf getDirectBodyBuf();
}
