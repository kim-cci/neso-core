package org.neso.core.request;

import org.neso.core.server.ServerContext;

public interface Session {

	/**
	 * 접속지 정보
	 */
    public String getRemoteAddr();
    
    /**
     * 접속한 시간
     */
    public long getConnectionTime();
    
    /**
     * 접속 서버 context
     */
    public ServerContext getServerContext();


    public void addAttribute(String key, Object value);

	public <T> T getAttribute(String key);

	public boolean removeAttrubute(String key);
}
