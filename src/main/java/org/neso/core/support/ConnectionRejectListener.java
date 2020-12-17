package org.neso.core.support;

public interface ConnectionRejectListener {
	
	/**
	 * 
	 * 접속 거부 리스트
	 * 
	 * @param maxConnections 현재 설정된 최대 접속 수
	 * @param remoteAddr 요청지 정보
	 * @return 접속 거절 시, 클라이언트에게 내려줄 메세지
	 */
	byte[] onConnectionReject(int maxConnections, String remoteAddr);
}
