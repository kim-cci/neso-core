package org.neso.core.request;

import org.neso.core.netty.ByteBasedWriter;

/**
 * 
 *
 */
public interface Client extends Session {
 
	/**
	 * 
	 * @return 커넥션 유지 여부
	 */
	public boolean isConnected();
	
	/**
	 * 클라이언트 접속 끊기
	 * client lock 점유가 종료되거나 없을 경우 disconnect 실행 
	 */
	public void disconnect();
	    
	/**
	 * 클라이언트에게 쓰기 가능한 writer객체 얻기
	 * {@link ByteBasedWriter}
	 * 
	 * 하나의 스레드(io스레드와 request처리 스레드들 중)의 쓰기 작업이 완료될때까지 점유 획득
	 * 
	 * writer.close() 혹은 releaseWriterLock 점유 반환
	 * @return writer
	 */
	public ByteBasedWriter getWriter();
	
	
	
	public void releaseWriterLock();
}