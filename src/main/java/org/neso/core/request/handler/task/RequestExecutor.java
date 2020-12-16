package org.neso.core.request.handler.task;


public interface RequestExecutor {
	
	public boolean isRunIoThread();
	
	public int getMaxRequets();
	
	public void init(int maxCount);
	
	public boolean registerTask(RequestTask task);
	
	public void shutdown();
}
