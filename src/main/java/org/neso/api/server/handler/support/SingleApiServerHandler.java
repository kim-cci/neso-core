package org.neso.api.server.handler.support;

import java.util.Arrays;

import org.neso.api.Api;
import org.neso.api.server.handler.AbstractWirableServerHandler;
import org.neso.core.exception.HeaderParsingException;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.HeadRequest;

/**
 * 단일 API이고, Head 필드에 body에 대한 길이값만 가지고 있고 서버 핸들러
 * 
 *  HEAD 3   |   BODY 12
 * [0][1][2]  [b][b][b][b][b][b][b][b][b][b][b][b]
 *             
 * 
 * headLength = 3
 */
public class SingleApiServerHandler extends AbstractWirableServerHandler {

	final private int headLength;
	
	public SingleApiServerHandler(int headLength, Api api) {
		this.headLength = headLength;
		registApi("_SINGLE_API", api);
	}
	
	@Override
	public int headLength() {
		return headLength;
	}
	
	@Override
	public int bodyLength(HeadRequest request) {
		try {
			return Integer.parseInt(new String(Arrays.copyOfRange(request.getHeadBytes(), 0, headLength)));
		} catch (Exception e) {
			throw new HeaderParsingException("invalid body length", request.getHeadBytes(), e);
		}
	}
	
	@Override
	public String apiKey(HeadBodyRequest request) {
		return "_SINGLE_API";
	}
}
