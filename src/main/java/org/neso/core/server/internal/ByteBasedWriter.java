package org.neso.core.server.internal;

import ch.qos.logback.core.net.server.Client;
import io.netty.buffer.ByteBuf;

/**
 * 바이트 단위로 쓰는 writer
 * 
 * {@link Client} getWriter method
 */
public interface ByteBasedWriter {

	public ByteBasedWriter write(byte b);
	
	public ByteBasedWriter write(byte[] bytes);
	
	public ByteBasedWriter write(ByteBuf buf);
	
	public void close();
}
