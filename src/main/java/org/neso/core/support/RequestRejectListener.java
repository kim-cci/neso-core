package org.neso.core.support;

import org.neso.core.request.HeadBodyRequest;
import org.neso.core.server.ServerContext;

public interface RequestRejectListener {

	
	/**
	 * request 요청 처리 거절 리스너
	 * 
	 * @param serverContext 서버 컨텍스트
	 * @param maxTaskThreads 설정된 최대 요청 수
	 * @param request 요청 정보
	 * @return 요청 거절 시, 클라이언트에게 내려줄 메세지
	 */
	byte[] onRequestReject(ServerContext serverContext, int maxTaskThreads, HeadBodyRequest request);
}
