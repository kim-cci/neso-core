package org.neso.core.request.handler;


import java.nio.charset.Charset;

import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRequestHandler implements RequestHandler {


	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Charset serverCharSet = Charset.defaultCharset();
	
	
	public void setCharset(Charset charSet) {
		this.serverCharSet = charSet;
	}
	
	@Override
	public Charset getCharset() {
		return serverCharSet;
	}

	
	@Override
	public void init(ServerContext context) {
		
	}

	@Override
	public void destroy() {
		
	}
	
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
