package org.neso.api.server.handler;

import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.Session;
import org.neso.core.server.ServerContext;

public abstract class ServerHandlerAdapter extends ServerHandler {


	@Override
	public void init(ServerContext context) {
	}

	@Override
	public void onConnect(Client client) {
	}

	@Override
	public void onDisconnect(Client client) {
	}

	@Override
	protected byte[] preApiExecute(Session session, HeadBodyRequest request) {
		return null;
	}

	@Override
	protected byte[] postApiExecute(Session session, HeadBodyRequest request, byte[] response) {
		return null;
	}

	@Override
	protected byte[] exceptionCaughtRequestIO(Session session, Throwable exception) {
		return null;
	}

	@Override
	protected byte[] exceptionCaughtDoRequest(Session session, HeadBodyRequest request, Throwable exception) {
		return null;
	}
}
