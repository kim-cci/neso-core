package org.neso.core.server.request.task;

/**
 * 요청 처리기
 * 
 * - 최대 동시 처리 숫자 관리
 * - shutdown시 요청 처리 관리
 * 
 * io스레드에 의해 공유되므로 Thread - safe하도록 구현
 * 
 *
 */
public interface RequestExecutor {
	
	public boolean isRunOnIoThread();
	
	public void init(int allowMaxRequests, int shutdownWaitSeconds);
	
	public boolean registerTask(RequestTask task);
	
	public void shutdown();
}
