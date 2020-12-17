package org.neso.core.netty;

import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import org.neso.core.request.Client;
import org.neso.core.server.ServerContext;

/**
 * Session 구현체
 * client 구현체이지만 session기능만 정의
 * 
 * {@link ClientAgent} 
 */
public abstract class SessionImplClient implements Client {

	final private long connectionTime;
	final private ServerContext serverContext;
	   
	final private SocketChannel sc;
		
	private Map<String, Object> sessionAttrMap = new LinkedHashMap<String, Object>();
	  
	public SessionImplClient(SocketChannel sc, ServerContext serverContext) {
		this.connectionTime = System.currentTimeMillis();
    	this.serverContext = serverContext;
    	this.sc = sc;
	}
    
	@Override
    public long getConnectionTime() {
    	return this.connectionTime;
    }
    
	@Override
	public ServerContext getServerContext() {
		return this.serverContext;
	}
	
	@Override
	public String getRemoteAddr() {
    	if (sc.remoteAddress() instanceof InetSocketAddress) {
        	InetSocketAddress addr = (InetSocketAddress) sc.remoteAddress();
        	return addr.getHostName();
    	} else {
    		return sc.remoteAddress().toString();
    	}
    }
	
	@Override
	public void addAttribute(String key, Object value) {
		sessionAttrMap.put(key, value);
	}
	    
    @SuppressWarnings("unchecked")
	@Override
    public <T> T getAttribute(String key) {
    	if (sessionAttrMap.containsKey(key)) {
        	return (T) sessionAttrMap.get(key);
    	} else {
    		return null;
    	}
    }
 
    @Override
    public boolean removeAttrubute(String key) {
    	if (sessionAttrMap.containsKey(key)) {
        	return sessionAttrMap.remove(key) != null ? true : false;
    	} else {
    		return false;
    	}
    }
	    
    
    protected SocketChannel socketChannel() {
		return sc;
	}
}
