package org.neso.core.request.factory;

import org.neso.core.request.Client;
import org.neso.core.request.internal.OperableHeadBodyRequest;

/**
 * request 생성 팩토리
 *
 */
public interface RequestFactory {
	
	public OperableHeadBodyRequest newHeadBodyRequest(Client client);
}
