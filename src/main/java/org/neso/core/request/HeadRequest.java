package org.neso.core.request;


public interface HeadRequest {
	
	public Session getSession();

	public long getRequestTime();

	public byte[] getHeadBytes();
	
	
	public void addAttribute(String key, Object value);
	
	public boolean removeAttribute(String key);
	
	public <T> T getAttribute(String key);
}
