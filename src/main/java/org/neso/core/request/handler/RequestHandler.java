package org.neso.core.request.handler;

import java.nio.charset.Charset;

import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.HeadRequest;
import org.neso.core.server.ServerContext;

public interface RequestHandler {
	
	public Charset getCharset();
	
	public void init(ServerContext context);
	
	/**
	 * 클라이언트 접속 시 호출
	 * @param client
	 */
	public void onConnect(Client client);
	
	
	/**
	 * 읽어야할 헤더 길이 반환
	 * 
	 * @return 읽어야할 바이트 길이
	 */
	public int headLength();	
    
	
	/**
	 * 읽어야할 바디 길이 반환
	 * 
	 * @param HeadRequest
	 * @return 읽어야할 바디 길이
	 */
    public int bodyLength(HeadRequest request);
    
    /**
     * 헤더와 바디를 모두 읽으면 호출
     * @param request
     * @return 처리결과 byte array
     */
    public void onRequest(Client client, HeadBodyRequest request);
    
    /**
     * request 처리  준비가 되면 호출 
     * @param client 요청 클라이언트
     * @param request 요청
     */
    public void doRequest(Client client, HeadBodyRequest request) throws Exception;
    
    
    /**
     * 접속 종료시 호출
     * @param client
     */
    public void onDisconnect(Client client);
    
    
    /**
     * 요청처리중 예외가 발생하면 호출
     * @param request 예외가 발생한 request
     * @param exception
     */
    public void onExceptionDoRequest(Client client, HeadBodyRequest request, Throwable exception);

    /**
     * 클라이언트로와 read 처리 중 예외가 발생하면 호출된다.
     * 
     * @param client 예외가 발생한 client
     * @param exception
     */
    public void onExceptionRead(Client client, Throwable exception);
    
    /**
     * 클라이언트로와 write 처리 중 예외가 발생하면 호출된다.
     * 
     * @param client 예외가 발생한 client
     * @param exception
     */
    public void onExceptionWrite(Client client, Throwable exception);

}
