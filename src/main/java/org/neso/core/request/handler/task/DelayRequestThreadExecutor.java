package org.neso.core.request.handler.task;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 지연 요청 처리 스레드 정책 
 * 
 * 상시 스레드 = 최대 스레드 
 * 대기큐 = 최대 스레드/ 2
 * 
 * 상시 스레드를 초과할 경우, 대기큐에서 대기
 */
public class DelayRequestThreadExecutor extends AbstractRequestExecutor {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ThreadPoolExecutor tp;

	
	@Override
	public boolean isRunOnIoThread() {
		return false;
	}

	@Override
	public void init(int maxRequests, int shutdownWaitSeconds) {
		super.init(maxRequests, shutdownWaitSeconds);
		
		this.tp = new ThreadPoolExecutor(getMaxRequets(),  getMaxRequets(), 
				0l, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(getMaxRequets() / 2)
				, new DefaultThreadFactory(getClass(), true));
	}
	
	@Override
	public boolean registerTask(RequestTask task) {
		try {
			tp.submit(task);
			return true;
		} catch (RejectedExecutionException ree) {
			return false;
		}
	}
	
	@Override
	public void shutdown() {
		//TODO 검증
		int notCompleted = tp.getActiveCount() + tp.getQueue().size();
		logger.info("requestTaskExecutor shutdown (queued task({}) + active task({}))", tp.getQueue().size(), tp.getActiveCount());
		if (notCompleted == 0) {
			return;
		}
		
		tp.shutdown();
		try {
			if (getShutdownWaitSeconds() == 0) {
				tp.shutdownNow();
				logger.info("Attempts to stop all({}) actively executing tasks", notCompleted);
			} else {	
			
				tp.shutdown();
				boolean shutdownResult = false;
				if (getShutdownWaitSeconds() == -1) {
					//무제한 대기
					shutdownResult = tp.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
				} else {
					shutdownResult = tp.awaitTermination(getShutdownWaitSeconds(), TimeUnit.SECONDS);
				}
				
				if (shutdownResult) {
					logger.info("request executor shutdown .. All requests were processed normally");
				} else {
					logger.info("request executor shutdown .. Not all requests have been fully processed");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
