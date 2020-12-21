package org.neso.core.request.handler.task;

/**
 * 
 * io스레드에 의해 공유되기 때문에 Thread - safe하게 구현해야 함
 *
 */
public interface RequestExecutor {
	
	public boolean isRunOnIoThread();
	
	public void init(int allowMaxRequests, int shutdownWaitSeconds);
	
	public boolean registerTask(RequestTask task);
	
	public void shutdown();
}
