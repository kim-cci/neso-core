package org.neso.core.request.handler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRequestExecutor implements RequestExecutor {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final int BEFORE_INITIALIZE = 0;
	
	private int maxRequets = BEFORE_INITIALIZE;
	private int shutdownWaitSeconds;
	

	public int getMaxRequets() {
		return this.maxRequets;
	}
	
	public int getShutdownWaitSeconds() {
		return this.shutdownWaitSeconds;
	}
	
	@Override
	public void init(int maxRequests, int shutdownWaitSeconds) {
		if (maxRequets != BEFORE_INITIALIZE) {
			//final 대체 - 설정 변경 불가
			logger.warn("request Executor has already been set");
			return;
		}
		
		if (maxRequests < 1) {
			throw new RuntimeException("max maxRequests more than zero");
		}

		if (shutdownWaitSeconds < -1) {
			throw new RuntimeException("shutdownWaitSeconds -1:nolimit, 0:immediately, 1 ~ : wait seconds");
		}
		this.maxRequets = maxRequests;
		this.shutdownWaitSeconds = shutdownWaitSeconds;
	}
}
