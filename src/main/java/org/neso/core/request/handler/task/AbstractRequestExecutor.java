package org.neso.core.request.handler.task;

public abstract class AbstractRequestExecutor implements RequestExecutor {

	private int maxRequets;
	
	@Override
	public int getMaxRequets() {
		return this.maxRequets;
	}
	
	@Override
	public void init(int maxExecuteSize) {
		if (maxExecuteSize < 1) {
			throw new RuntimeException("max count more than zero");
		}
		this.maxRequets = maxExecuteSize;
	}
}
