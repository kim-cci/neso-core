package org.neso.core.server.internal;

import io.netty.buffer.ByteBuf;

/**
 * 바이트 단위로 쓰는 writer
 */
public interface ByteBasedWriter {

	public void write(byte b);
	
	public void write(byte[] bytes);
	
	public void write(ByteBuf buf);
	
	public void close();
}
