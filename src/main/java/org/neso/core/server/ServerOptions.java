package org.neso.core.server;

import org.neso.core.server.request.task.BasicRequestThreadExecutor;
import org.neso.core.server.request.task.RequestExecutor;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;

/**
 * 서버 실행 옵션 추상화
 * {@link Server}
 *
 */
public abstract class ServerOptions implements ServerUI {
	
	
	/**
	 * 서버 옵션과 기본값 정의
	 */
	private int maxConnections = -1;	//최대 동시 접속 수
	
	private int maxRequests = 100;	//최대 동시 요청 수
	
	private boolean connectionOriented = false;	//접속 유지 여부
	
	private int readTimeoutMillis = 5000;	//read timeout
	
	private int writeTimeoutMillis = 2000;
	
	private int maxRequestBodyLength = -1;	//본문 최대 길이
   
	private boolean inoutLogging = false;
	
	private LogLevel pipeLineLogLevel  = null;
	
	private Class<? extends RequestExecutor> requestExecutorType = BasicRequestThreadExecutor.class;
	
	private int requestExecutorshutdownWaitSeconds = -1;
	
	
	
	
	
	
	public Class<? extends RequestExecutor> getRequestExecutorType() {
		return requestExecutorType;
	}
	
	public boolean isConnectionOriented() {
		return connectionOriented;
	}

	public int getMaxRequests() {
		return maxRequests;
	}
	
	public int getReadTimeoutMillis() {
		return readTimeoutMillis;
	}
	
	public int getMaxConnections() {
		return maxConnections;
	}
	
	public int getMaxRequestBodyLength() {
		return maxRequestBodyLength;
	}
	
	public boolean isInoutLogging() {
		return inoutLogging;
	}
	
	public LogLevel getPipeLineLogLevel() {
		return pipeLineLogLevel;
	}
	
	public int getWriteTimeoutMillis() {
		return writeTimeoutMillis;
	}

	public int getRequestExecutorshutdownWaitSeconds() {
		return requestExecutorshutdownWaitSeconds;
	}
	
	/**
	 * -1 (요청 완료까지 대기) : default
	 * 0 : 요청 처리 중지하고 바로 종료
	 * 1 ~ : 설정된 Second만큼 대기 후 종료
	 * @param requestExecutorshutdownWaitSeconds 셧다운시 request 처리 대기 시간
	 * @return ServerOptions options
	 */
	public ServerOptions requestExecutorshutdownWaitSeconds(int requestExecutorshutdownWaitSeconds) {
		this.requestExecutorshutdownWaitSeconds = requestExecutorshutdownWaitSeconds;
		return this;
	}
	
	/**
	 * default : false
	 * @param connectionOriented 접속 유지 여부
	 * @return ServerOptions options
	 */
	public ServerOptions connectionOriented(boolean connectionOriented) {
		this.connectionOriented = connectionOriented;
		return this;
	}
	
	/**
	 * default : 100
	 * @param maxRequests 최대 요청 갯 수
	 * @return ServerOptions
	 */
	public ServerOptions maxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
		return this;
	}

	/**
	 * default : BasicRequestThreadExecutor.class
	 * @param requestExecutorType 요청처리실행기 타입
	 * @return ServerOptions
	 */
	public ServerOptions requestExecutorType(Class<? extends RequestExecutor> requestExecutorType) {
		this.requestExecutorType = requestExecutorType;
		return this;
	}
	
	/**
	 * default : 5000 (5 Second)
	 * @param readTimeoutMillis readTimeout milli second
	 * @return ServerOptions
	 */
	public ServerOptions readTimeoutMillis(int readTimeoutMillis) {
		this.readTimeoutMillis = readTimeoutMillis;
		return this;
	}
	
	/**
	 * default : 2000 (2 Second)
	 * @param writeTimeoutMillis writeTimeout milli second
	 * @return ServerOptions
	 */
	public ServerOptions writeTimeoutMillis(int writeTimeoutMillis) {
		if (writeTimeoutMillis < 1) {
	  		throw new RuntimeException("writeTimeoutMillis is bigger than zero");
	  	}
	  	this.writeTimeoutMillis = writeTimeoutMillis;
		return this;
	}

	/**
	 * default : -1 (제한 없음)
	 * @param maxConnections 최대 접속 수
	 * @return ServerOptions
	 */
	public ServerOptions maxConnections(int maxConnections) {
		if (maxConnections < 1) {
	  		throw new RuntimeException("maxConnections is bigger than zero");
	  	}
		this.maxConnections = maxConnections;

		return this;
	}

	/**
	 * default : -1 (제한 없음)
	 * @param maxRequestBodyLength 최대 요청 수
	 * @return ServerOptions
	 */
	public ServerOptions maxRequestBodyLength(int maxRequestBodyLength) {
		this.maxRequestBodyLength = maxRequestBodyLength;
		return this;
	}

	/**
	 * default : false
	 * @param inoutLogging 입출력 로그 활성화 여부
	 * @return ServerOptions
	 */
	public ServerOptions inoutLogging(boolean inoutLogging) {
		this.inoutLogging = inoutLogging;
		return this;
	}
	
	/**
	 * default : null (로깅하지 않음)
	 * @param pipeLineLogLevel pipeline 로그 출력 여부
	 * @return ServerOptions
	 */
	public ServerOptions pipeLineLogLevel(LogLevel pipeLineLogLevel) {
		this.pipeLineLogLevel = pipeLineLogLevel;
		return this;
	}
	

// 	-Deprecated
//  	public Server requestExecutorType(Class<? extends RequestExecutor> executorClz, Class<?>... parameterTypes) {
//  		this.requestExecutorType = executorClz;
//  		return this;
//  	}
	
	
	public abstract <T> ServerOptions channelOption(ChannelOption<T> option, T value);
    
	public abstract <T> ServerOptions childChannelOption(ChannelOption<T> childOption, T value);
	
}
