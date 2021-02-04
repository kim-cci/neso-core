package org.neso.core.request.factory;

import org.neso.core.request.Client;
import org.neso.core.request.internal.InMemoryHeadBodyRequest;
import org.neso.core.request.internal.OperableHeadBodyRequest;

/**
 * 메모리 기반 request 생성 factory
 */
public class InMemoryRequestFactory implements RequestFactory {
 
	@Override
	public OperableHeadBodyRequest newHeadBodyRequest(Client client) {
		return new InMemoryHeadBodyRequest(client);
	}	
}
