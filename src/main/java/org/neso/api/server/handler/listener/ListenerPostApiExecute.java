package org.neso.api.server.handler.listener;

import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.Session;

/**
 * API 후 처리
 *
 */
public interface ListenerPostApiExecute {

	/**
	 * @return null이 아닐 경우, 처리결과 response 대신  return값으로 응답을 내려준다.
	 */
	public byte[] event(Session session, HeadBodyRequest request, byte[] response);
}
