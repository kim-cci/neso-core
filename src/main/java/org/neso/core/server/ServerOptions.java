package org.neso.core.server;

import org.neso.core.server.request.task.BasicRequestThreadExecutor;
import org.neso.core.server.request.task.RequestExecutor;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;

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
	
	public ServerOptions requestExecutorshutdownWaitSeconds(int requestExecutorshutdownWaitSeconds) {
		this.requestExecutorshutdownWaitSeconds = requestExecutorshutdownWaitSeconds;
		return this;
	}
	
	
	public ServerOptions connectionOriented(boolean connectionOriented) {
		this.connectionOriented = connectionOriented;
		return this;
	}

	public ServerOptions maxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
		return this;
	}

	public ServerOptions requestExecutorType(Class<? extends RequestExecutor> requestExecutorType) {
		this.requestExecutorType = requestExecutorType;
		return this;
	}
	
	public ServerOptions readTimeoutMillisOnRead(int readTimeoutMillis) {
		this.readTimeoutMillis = readTimeoutMillis;
		return this;
	}

	public ServerOptions writeTimeoutMillis(int writeTimeoutMillis) {
		if (writeTimeoutMillis < 1) {
	  		throw new RuntimeException("writeTimeoutMillis is bigger than zero");
	  	}
	  	this.writeTimeoutMillis = writeTimeoutMillis;
		return this;
	}

	public ServerOptions maxConnections(int maxConnections) {
		if (maxConnections < 1) {
	  		throw new RuntimeException("maxConnections is bigger than zero");
	  	}
		this.maxConnections = maxConnections;

		return this;
	}

	public ServerOptions maxRequestBodyLength(int maxRequestBodyLength) {
		this.maxRequestBodyLength = maxRequestBodyLength;
		return this;
	}

	public ServerOptions inoutLogging(boolean inoutLogging) {
		this.inoutLogging = inoutLogging;
		return this;
	}
	
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
