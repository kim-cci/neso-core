package org.neso.core.request.factory;

import org.neso.core.request.Client;
import org.neso.core.request.internal.OperableHeadBodyRequest;

/**
 * 파일 기반 request 팩토리
 *
 */
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
