package org.neso.core.exception;

@SuppressWarnings("serial")
public class OverReadBytesException extends RuntimeException {

	final byte[] overBytes;
	
	public OverReadBytesException(byte[] overBytes) {
		super("over read from request .. over length = " + overBytes.length);
		this.overBytes = overBytes;
	}
}
