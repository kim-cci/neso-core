package org.neso.core.server;

import org.neso.core.request.factory.RequestFactory;
import org.neso.core.request.handler.RequestHandler;
import org.neso.core.request.handler.task.RequestExecutor;
import org.neso.core.support.ConnectionManager;

public class ServerContext {
	
	final private int port;
	final private RequestHandler requestHandler;
	
	final ServerOptions options;
	
	private RequestFactory requestFactory;
	private RequestExecutor requestExecutor;
	private ConnectionManager connectionManager;
	
	
	
	public ServerContext(int port, RequestHandler requestHandler, ServerOptions serverOptions) {
		this.port = port;
		this.requestHandler = requestHandler;
		this.options = serverOptions;
	}
	
	public int port() {
		return this.port;
	}
	
	public RequestHandler requestHandler() {
		return this.requestHandler;
	}
	
	
	public RequestFactory requestFactory() {
		return this.requestFactory;
	}
	
	public RequestExecutor requestExecutor() {
		return this.requestExecutor;
	}
	
	public ServerOptions options() {
		return this.options;
	}
	
	public ConnectionManager connectionManager() {
		return this.connectionManager;
	}
	
	protected void setRequestFactory(RequestFactory requestFactory) {
		this.requestFactory = requestFactory;
	}
	
	protected void setRequestExecutor(RequestExecutor requestExecutor) {
		this.requestExecutor = requestExecutor;
	}
	
	protected void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
}
