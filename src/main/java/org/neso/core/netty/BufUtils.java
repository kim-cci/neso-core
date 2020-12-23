package org.neso.core.netty;

import static io.netty.util.internal.StringUtil.NEWLINE;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class BufUtils {
    
    public static byte[] copyToByteArray(ByteBuf buf) {
        byte[] temp = new byte[buf.readableBytes()];
        buf.readBytes(temp);
        return temp;
    }
    
    public static String bufToString(String eventName, ByteBuf buf) {
		int length = buf.readableBytes();
		int offset = buf.readerIndex();
        int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
        StringBuilder dump = new StringBuilder(eventName.length() + 2 + 10 + 1 + 2 + rows * 80);

        dump.append(eventName).append(": ").append(length).append('B').append(NEWLINE);
    	ByteBufUtil.appendPrettyHexDump(dump, buf, offset, length);
    	
    	buf.resetReaderIndex();
    	
    	return dump.toString();
    }
}
