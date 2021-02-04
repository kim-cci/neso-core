package org.neso.core.support;

/**
 * 접속 거절 리스너
 * 
 * 최대 동시 요청 수를 초과할 경우 발생
 * 
 * ..... extends ServerHandlerAdapter implements ConnectionRejectListener
 */
public interface ConnectionRejectListener {
	
	/**
	 * @param maxConnections 현재 설정된 최대 접속 수
	 * @param remoteAddr 요청지 정보
	 * @return 접속 거절 시, 클라이언트에게 내려줄 메세지
	 */
	byte[] onConnectionReject(int maxConnections, String remoteAddr);
}
