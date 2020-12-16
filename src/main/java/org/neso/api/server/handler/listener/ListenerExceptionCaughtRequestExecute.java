package org.neso.api.server.handler.listener;

import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.Session;

/**
 * API 예외 처리기
 */
public interface ListenerExceptionCaughtRequestExecute {
	
	/**
	 * @param exception 발생한 예외
	 * @return 예외 발생 시 내려줄 응답, null리턴시, 기본 메세지 호출
	 */
	public byte[] event(Session session, HeadBodyRequest request, Throwable exception);
}
