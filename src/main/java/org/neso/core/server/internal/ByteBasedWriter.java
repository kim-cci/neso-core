package org.neso.core.server.internal;

import io.netty.buffer.ByteBuf;

/**
 * 바이트 단위로 쓰는 writer
 * 
 * {@link Client} getWriter()
 */
public interface ByteBasedWriter {

	public ByteBasedWriter write(byte b);
	
	public ByteBasedWriter write(byte[] bytes);
	
	public ByteBasedWriter write(ByteBuf buf);
	
	public void close();
}
