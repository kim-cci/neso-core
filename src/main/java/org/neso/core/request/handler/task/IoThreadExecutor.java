package org.neso.core.request.handler.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class IoThreadExecutor extends AbstractRequestExecutor {
	
	private AtomicInteger inProgressRequestTaskCount = new AtomicInteger(0);
	
	private Map<Long, Thread> ingThreadMap = new ConcurrentHashMap<Long, Thread>();
	
	private volatile boolean shutdownIng = false;
	
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
		
		
		if (getShutdownWaitSeconds() == -1) {
			
			task.run();
		} else {
			if (this.shutdownIng) {
				return false;
			}

			Thread th = Thread.currentThread();
			ingThreadMap.put(th.getId(), th);
			task.run();
			if (!this.shutdownIng) {
				ingThreadMap.remove(th.getId());
			}
		}
		
		inProgressRequestTaskCount.decrementAndGet();
		return true;
	}

	@Override
	public void shutdown() {
		//to-do 확인, io스레드를 이렇게 죽여도 되나.
		
		if (getShutdownWaitSeconds() == 0) {
			//즉시 종료
			forseStopIoThread();
		} else if (getShutdownWaitSeconds() == -1) {
			//무제한 대기
			//Server .. shutdownGracefully
		} else {	
			
			//1초마다 요청 처리 완료되었는지 확인하고 허용시간초과하면 바로 종료
			for (int i = 0; i < getShutdownWaitSeconds() ; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (ingThreadMap.size() == 0) {
					return;
				}
			}

			
			forseStopIoThread();
		}
	}
	
	private void forseStopIoThread() {
		shutdownIng = true;
		int notCompleted = ingThreadMap.size();
		logger.info("canceled request tasks... ({})", ingThreadMap.toString());
		for (Thread ioThread : ingThreadMap.values()) { //반복문 도는 중 remove시 오류 방지는 registerTask 처리
			ioThread.interrupt();
		}
		logger.info("canceled request tasks... ({})", notCompleted);
	}
}
