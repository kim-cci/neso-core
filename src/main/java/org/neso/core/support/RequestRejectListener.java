package org.neso.core.support;

import org.neso.core.request.HeadBodyRequest;
import org.neso.core.server.ServerContext;

public interface RequestRejectListener {

	
	/**
	 * request 요청 처리 거절 리스너
	 * 
	 * @return 접속 거절 시, 클라이언트에게 내려줄 메세지
	 */
	byte[] onRequestReject(ServerContext serverContext, int maxTaskThreads, HeadBodyRequest request);
}
