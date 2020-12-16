package org.neso.core.request;

import io.netty.buffer.ByteBuf;

public interface HeadBodyRequest extends HeadRequest {

	/**
	 * Body byte array 
	 * @return
	 */
	public byte[] getBodyBytes(); 
	
	/**
	 * Head + body byte array
	 * @return
	 */
	public byte[] getAllBytes();
	
	/**
	 * 
	 */
	public ByteBuf getDirectBodyBuf();
}
