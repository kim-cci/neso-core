package org.neso.core.request.handler.task;

import java.util.concurrent.atomic.AtomicInteger;

public class IoThreadExecutor extends AbstractRequestExecutor {
	
	private AtomicInteger inProgressRequestTaskCount = new AtomicInteger(0);
	
	@Override
	public boolean isRunOnIoThread() {
		return true;
	}
	
	
	@Override
	public boolean registerTask(RequestTask task) {
		
		if (inProgressRequestTaskCount.incrementAndGet() > getMaxRequets()) {
			inProgressRequestTaskCount.decrementAndGet();
			return false;
		}
		
		task.run();
		inProgressRequestTaskCount.decrementAndGet();
		return true;
	}

	@Override
	public void shutdown() {
		
	}
}
