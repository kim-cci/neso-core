package org.neso.core.netty;

import io.netty.buffer.ByteBuf;

public class BufUtils {
    
    public static byte[] copyToByteArray(ByteBuf buf) {
        byte[] temp = new byte[buf.readableBytes()];
        buf.readBytes(temp);
        return temp;
    }
}
