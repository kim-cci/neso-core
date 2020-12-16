package org.neso.api;

import org.neso.core.request.HeadBodyRequest;

/**
 * request 처리 핸들러
 */
public interface Api {
	
    public byte[] handle(HeadBodyRequest request) throws Exception;
    
}