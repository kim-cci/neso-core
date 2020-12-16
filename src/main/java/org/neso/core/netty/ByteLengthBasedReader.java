package org.neso.core.netty;

import io.netty.buffer.ByteBuf;

/**
 * 
 * @See {@link ByteLengthBasedInboundHandler}
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
	
	public ReaderStatus getStatus();

	/**
	 * 읽어야 할 바이트 길이 조회
	 * 
	 * @return
	 */
	public int getToReadBytes();
	    
	/**
	 * 
	 * @param readedBuf
	 * @return boolean 한 사이클이 완료되었는지 ( reader에서 .. 이상하지만...readtimeHandler책임을 분산시키는게 이상해서.. 두지 않으려면 )
	 * @throws Exception
	 */
	public void onRead(ByteBuf readedBuf) throws Exception;
	
	/**
	 * 읽기가 종료되었는지
	 * @return
	 */
	
	public void init();
	
	public void destroy();
	
	public void onReadException(Throwable th);
}
