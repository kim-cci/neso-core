package org.neso.core.server.request.task;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 기본 요청 처리기(스레드풀 사용) 
 * 
 * 스레드풀 정책:
 * 상시 스레드는 jvm이용가능 코어 수 * 2
 * 대기큐 없음
 * 5초후 임시 스레드 반환
 * 상시 스레드 초과시, 바로 임시스레드 투입 
 */
public class BasicRequestThreadExecutor extends AbstractRequestExecutor {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private volatile boolean shutdownIng = false;
	
	private ThreadPoolExecutor tp;

 
	@Override
	public boolean isRunOnIoThread() {
		return false;
	}

	@Override
	public void init(int maxRequests, int shutdownWaitSeconds) {
		super.init(maxRequests, shutdownWaitSeconds);
		
		int base = getMaxRequets() / 10;
		
		int coreThreadSize = base - (base % Runtime.getRuntime().availableProcessors());
		if (coreThreadSize > getMaxRequets()) {
			coreThreadSize = getMaxRequets();
		}
		this.tp = new ThreadPoolExecutor(coreThreadSize,  getMaxRequets(), 
				5000l, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>()
				, new DefaultThreadFactory(getClass(), true));
	}
	
	@Override
	public  boolean registerTask(RequestTask task) {
		
		if (shutdownIng) {
			return false;
		}
		
		try {
			tp.submit(task);
			return true;
		} catch (RejectedExecutionException ree) {
			return false;
		}
	}
	
	@Override
	public void shutdown() {
		shutdownIng = true;
		int notCompleted = tp.getActiveCount(); // + tp.getQueue().size();
		if (notCompleted == 0) {
			return;
		}
		try {
			if (getShutdownWaitSeconds() == 0) {
				//즉시 종료
				tp.shutdownNow();
				logger.info("canceled request tasks... ({})", notCompleted);
			} else if (getShutdownWaitSeconds() == -1) {
				//무제한 대기
				tp.shutdown();
			} else {	
				
				tp.shutdown();
				if (tp.awaitTermination(getShutdownWaitSeconds(), TimeUnit.SECONDS)) {
					logger.info("request executor shutdown .. All requests were processed normally");
				} else {
					logger.info("request executor shutdown .. Not all requests have been fully processed");
					tp.shutdownNow();
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
