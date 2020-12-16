package org.neso.core.exception;

import org.neso.core.request.Client;

@SuppressWarnings("serial")
public class ClientAbortException extends RuntimeException {

	private Client client;
	    
	public ClientAbortException(Client client) {
		super("can't response to client, ip=" + client.getRemoteAddr());
        this.client = client;
    }
    

    public Client getClient() {
    	return this.client;
    }
}
