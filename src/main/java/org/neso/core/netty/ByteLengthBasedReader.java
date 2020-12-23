package org.neso.core.netty;

import io.netty.buffer.ByteBuf;

/**
 * 
 * {@link ByteLengthBasedInboundHandler}
 * 
 * 
 * 1.getToReadByte
 * 2.onRead(toReadBuf) 
 * 
 */
public interface ByteLengthBasedReader {
	
	public enum ReaderStatus {
		
		STANBY, //읽기 전 상태 	(읽기 가능, 읽기 상태에 돌입하지 않음 => timeout 동작 x)
		ING, //읽는 도중 		(읽기 가능, 읽기 상태에 돌입함 => timeout 동작)  
		CLOSE //읽기가 끝난 상태 (읽기 불가)
	}
	
	public int getReadTimeoutMillis();
	
	public ReaderStatus getStatus();

	/**
	 * @return 읽어야할 바이트 길이
	 */
	public int getToReadBytes();
	    
	/**
	 * 
	 * @param readedBuf 읽은 버프
	 * @throws Exception 발생한 오류
	 */
	public void onRead(ByteBuf readedBuf) throws Exception;
	
	public void init();
	
	public void destroy();
	
	public void onReadException(Throwable th);
}
