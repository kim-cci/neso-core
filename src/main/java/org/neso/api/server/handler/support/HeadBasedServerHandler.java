package org.neso.api.server.handler.support;

import java.util.Arrays;

import org.neso.api.server.handler.AbstractWirableServerHandler;
import org.neso.core.exception.HeaderParsingException;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.HeadRequest;


/**
 * Head 필드에 body에 대한 길이값과 api 식별키(전문번호)를 헤더에서 가지고 있는 경우
 * 
 *                HEAD 18                                |   BODY 6
 * -------------------------------------------------------------------------
 *  바디길이 (4) |    YYYYMMDD(8)        |  전문번호(6)       |  
 *  ------------------------------------------------------------------------
 * [0][0][0][6][2][0][1][9][0][1][0][1][S][E][A][R][C][H] [b][b][b][b][b][b]
 * 
 * headLength = 18
 * bodyLengthFiledOffset = 0
 * bodyLengthFiledLength = 4
 * apiIdFieldOffset = 12
 * apiIdFieldLength = 6
 */

public class HeadBasedServerHandler extends AbstractWirableServerHandler {
	
	final private int headLength;
	final private int bodyLengthFiledOffset;
	final private int bodyLengthFiledLength;
	final private int apiIdFieldOffset;
	final private int apiIdFieldLength;
	

	public HeadBasedServerHandler(int headLength, int bodyLengthFiledOffset, int bodyLengthFiledLength, int apiIdFieldOffset, int apiIdFieldLength) {
		this.headLength = headLength;
		if (headLength < 0 || bodyLengthFiledOffset < 0 || bodyLengthFiledLength < 0 || apiIdFieldOffset < 0 || apiIdFieldLength < 0) {
			throw new IllegalArgumentException("value > 0");
		}
		this.bodyLengthFiledOffset = bodyLengthFiledOffset;
		this.bodyLengthFiledLength = bodyLengthFiledLength;
		this.apiIdFieldOffset = apiIdFieldOffset;
		this.apiIdFieldLength = apiIdFieldLength;
	}
	
	@Override
	public int headLength() {
		return headLength;
	}

	@Override
	public int bodyLength(HeadRequest request) {
		try {
			return Integer.parseInt(new String(Arrays.copyOfRange(request.getHeadBytes(), bodyLengthFiledOffset, (bodyLengthFiledOffset  + bodyLengthFiledLength))));
		} catch (Exception e) {
			throw new HeaderParsingException("invalid body length", request.getHeadBytes(), e);
		}
	}
	
	@Override
	public String apiKey(HeadBodyRequest request) {
		try {
			return new String(Arrays.copyOfRange(request.getHeadBytes(), apiIdFieldOffset, (apiIdFieldOffset + apiIdFieldLength)), getCharset()).trim();
		} catch (Exception e) {
			throw new HeaderParsingException("invalid api key", request.getHeadBytes(), e);
		}
	}
}
