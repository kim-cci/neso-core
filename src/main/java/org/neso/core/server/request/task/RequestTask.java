package org.neso.core.server.request.task;


import org.neso.core.request.Client;
import org.neso.core.request.handler.RequestHandler;
import org.neso.core.request.internal.OperableHeadBodyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RequestTask implements Runnable {
	 
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	final private OperableHeadBodyRequest request;
	final private Client requestClient;
	
	//private boolean interrupted = false;
	
	public RequestTask(Client client, OperableHeadBodyRequest request) {
		this.request = request; 
		this.requestClient = client;
	}
	
	
	public void run() {
		
		final RequestHandler requestHandler = requestClient.getServerContext().requestHandler();
		try {
			
			if (requestClient.isConnected()) {
				
				logger.debug("request task started.., {}", requestClient.toString());
				
				long startTime = System.currentTimeMillis();//System.nanoTime();
				
				requestHandler.doRequest(requestClient, request);

				long elapsedMilis = System.currentTimeMillis() - startTime; //System.nanoTime() - startTime;	
				
				//TODO //지연 리스터 처리 ?
				
				logger.debug("request task finished.. elapse time -> {}.{} sec {}", elapsedMilis / 1000, String.format("%03d", elapsedMilis % 1000), requestClient.toString());
			} else {
				logger.debug("request task stopped by disconnect, {}", requestClient.toString());
			}
			
		} catch (InterruptedException ignore) {
			//to-do 만약 IO Thread requestExcutor 일 경우 고민해보자.. 
			//interrupted = true;
		} catch (Exception e) {
			
			try {
				requestHandler.onExceptionDoRequest(requestClient, request, e);
				
			} catch (Exception e2) {
				logger.error("occurred requestHandler's exceptionCaughtRequestExecute.. client disconnect", e2);
				requestClient.disconnect();
			}
			
		} finally {
			
			
			request.release();
			requestClient.releaseWriterLock();
		}
	}
}


