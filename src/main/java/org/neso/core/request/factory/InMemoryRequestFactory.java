package org.neso.core.request.factory;

import org.neso.core.request.Client;
import org.neso.core.request.internal.InMemoryHeadBodyRequest;
import org.neso.core.request.internal.OperableHeadBodyRequest;

public class InMemoryRequestFactory implements RequestFactory {
 
	@Override
	public OperableHeadBodyRequest newHeadBodyRequest(Client client) {
		return new InMemoryHeadBodyRequest(client);
	}	
}
