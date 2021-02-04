package org.neso.core.request;

import org.neso.core.server.ServerContext;

/**
 * 접속(정보)에 대한 추상화
 *
 * 조작 불가하도록 set메소드는 Client에 분리 {@link Client} 
 */
public interface Session {

	/**
	 * @return 커넥션 유지 여부
	 */
	public boolean isConnected();
	
	/**
	 * @return 접속지 정보
	 */
    public String getRemoteAddr();
    
    /**
     * @return 접속한 시간
     */
    public long getConnectionTime();
    
    /**
     * @return 접속 서버 context
     */
    public ServerContext getServerContext();


    public void addAttribute(String key, Object value);

	public <T> T getAttribute(String key);

	public boolean removeAttrubute(String key);
}
