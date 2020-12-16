package org.neso.api.server.handler.listener;

import org.neso.core.request.Session;

public interface ListenerExceptionCaughtRequestIO {

	 public byte[] event(Session session, Throwable exception);
}
