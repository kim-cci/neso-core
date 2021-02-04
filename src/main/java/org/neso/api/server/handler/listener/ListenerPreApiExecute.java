package org.neso.api.server.handler.listener;

import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.Session;

/**
 * API 전 처리
 */
public interface ListenerPreApiExecute {
	
	/**
	 * @param session 접속 정보
	 * @param request 요청 정보
	 * @return null이 아닐 경우, Api를 실행하지 않고 return값으로 응답을 내려준다.
	 */
	 public byte[] event(Session session, HeadBodyRequest request);
}
