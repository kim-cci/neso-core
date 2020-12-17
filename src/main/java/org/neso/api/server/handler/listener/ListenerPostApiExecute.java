package org.neso.api.server.handler.listener;

import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.Session;

/**
 * API 후 처리
 *
 */
public interface ListenerPostApiExecute {

	/**
	 * 
	 * @param session 접속 정보
	 * @param request 요청 정보
	 * @param response Api처리 결과
	 * @return null이 아닐 경우, 처리결과 response 대신  return값으로 응답을 내려준다.
	 */
	public byte[] event(Session session, HeadBodyRequest request, byte[] response);
}
