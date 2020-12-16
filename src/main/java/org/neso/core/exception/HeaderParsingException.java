package org.neso.core.exception;

import java.util.Arrays;

@SuppressWarnings("serial")
public class HeaderParsingException extends RuntimeException {

    private byte[] headerBytes;
    
    public HeaderParsingException(String message, byte[] headerBytes, Throwable th) {
        super(message + ", ASCII : " + Arrays.toString(headerBytes), th);
        this.headerBytes = headerBytes;
    }
    
 
    
    public byte[] getHeaderBytes() {
    	return this.headerBytes;
    }
}
