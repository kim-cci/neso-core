package org.neso.core.request.handler;

import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;

public abstract class RequestHandlerAdapter extends AbstractRequestHandler {

	@Override
	public void onConnect(Client client) {
		
	}

	@Override
	public void onDisconnect(Client client) {
		
	}

	@Override
	public void onExceptionDoRequest(Client client, HeadBodyRequest request, Throwable exception) {
		client.disconnect();
	}

	@Override
	public void onExceptionRead(Client client, Throwable exception) {
		client.disconnect();
	}

	@Override
	public void onExceptionWrite(Client client, Throwable exception) {
		client.disconnect();
	}

}
