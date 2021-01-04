package org.neso.core.exception;

import java.util.Arrays;

//to-do 예외추상화
@SuppressWarnings("serial")
public class RequestReadException extends RuntimeException {

    private byte[] bytes;
    
    public RequestReadException(String message, byte[] bytes, Throwable th) {
        super(message + ", ASCII : " + Arrays.toString(bytes), th);
        this.bytes = bytes;
    }
    
 
    
    public byte[] getHeaderBytes() {
    	return this.bytes;
    }
}
