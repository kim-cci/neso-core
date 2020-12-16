package org.neso.core.support;

public interface ConnectionRejectListener {
	
	/**
	 * 접속 거부 리스트
	 * 
	 * @return 접속 거절 시, 클라이언트에게 내려줄 메세지
	 */
	byte[] onConnectionReject(int maxConnections, String remoteAddr);
}
