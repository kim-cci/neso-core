package org.neso.api.support;

import org.neso.api.Api;
import org.neso.core.request.HeadBodyRequest;

/**
 * 정적 응답 Api
 *
 */
public class StaticResponseApi implements Api {

	private byte[] fixed;
	
	public StaticResponseApi(String fixed) {
		this.fixed = fixed.getBytes();
	}
	
	public StaticResponseApi(byte[] fixed) {
		this.fixed = fixed;
	}
	
	@Override
	public byte[] handle(HeadBodyRequest request) throws Exception {
		return fixed;
	}
}
