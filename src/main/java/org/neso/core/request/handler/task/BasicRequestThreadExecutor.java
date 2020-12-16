package org.neso.core.request.handler.task;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 기본 요청 처리 스레드 정책 
 * 
 * 상시 스레드는 jvm이용가능 코어 수 * 2
 * 대기큐 없음
 * 5초후 임시 스레드 반환
 * 
 * 상시 스레드 초과시, 바로 임시스레드 투입 
 */
public class BasicRequestThreadExecutor extends AbstractRequestExecutor {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ThreadPoolExecutor tp;

 
	
	@Override
	public boolean isRunIoThread() {
		return false;
	}

	@Override
	public void init(int maxExecuteSize) {
		super.init(maxExecuteSize);
		
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
		logger.info("requestTaskThreadExecutor shutdown (current task count={})", tp.getTaskCount());
		tp.shutdown();
		try {
			if (!tp.isShutdown() && !tp.awaitTermination(5, TimeUnit.SECONDS)) {

				if (!tp.isShutdown() && !tp.awaitTermination(5, TimeUnit.SECONDS)) {
					logger.info("shutdown fail...");
				}
			}
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
}
