package org.neso.core.request.handler.task;

import java.util.concurrent.atomic.AtomicInteger;

public class IoThreadExecutor extends AbstractRequestExecutor {
	
	private AtomicInteger currentCount = new AtomicInteger(0);
	
	@Override
	public boolean isRunIoThread() {
		return true;
	}
	
	
	@Override
	public boolean registerTask(RequestTask task) {
		
		if (currentCount.incrementAndGet() > getMaxRequets()) {
			currentCount.decrementAndGet();
			return false;
		}
		
		task.run();
		currentCount.decrementAndGet();
		return true;
	}

	@Override
	public void shutdown() {
		
	}
}
