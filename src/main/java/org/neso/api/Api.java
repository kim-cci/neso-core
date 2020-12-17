package org.neso.api;

import org.neso.core.request.HeadBodyRequest;

/**
 * request 처리 핸들러
 * 
 * @author kim-cci
 *
 */
public interface Api {
	
	/**
	 * 
	 * @param request 요청정보
	 * @return api실행 결과 byte array
	 * @throws Exception 요청처리중 발생 예외
	 */
    public byte[] handle(HeadBodyRequest request) throws Exception;
    
}