package org.neso.core.request.factory;

import org.neso.core.request.Client;
import org.neso.core.request.internal.OperableHeadBodyRequest;

public class InFileRequestFactory implements RequestFactory {

	String rootPath = null;
	
	public InFileRequestFactory(String path, boolean repeatableRequest) {
		this.rootPath = path;
	}
	
	
	
	@Override
	public OperableHeadBodyRequest newHeadBodyRequest(Client client) {
		return null;
	}
}
