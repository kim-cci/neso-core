package org.neso.core.request.handler.task;


import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.internal.OperableHeadBodyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestTask implements Runnable {
	 
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	final private OperableHeadBodyRequest request;
	final private Client client;
	
	public RequestTask(Client client, OperableHeadBodyRequest request) {
		this.request = request; 
		this.client = client;
	}
	
	public HeadBodyRequest getHeadBodyRequest() {
		return this.request;
	}
	
	public Client getClient() {
		return this.client;
	}
	
	
	public void run() {
		try {
			
			if (client.isConnected()) {
				
				logger.debug("request task started.., {}", client.toString());
				
				long startTime = System.currentTimeMillis();//System.nanoTime();
				
				client.getServerContext().requestHandler().doRequest(client, request);

				long elapsedMilis = System.currentTimeMillis() - startTime; //System.nanoTime() - startTime;	
				
				//TODO //지연 리스터 처리 ?
				
				logger.debug("request task finished.. elapse time -> {}.{} sec {}", elapsedMilis / 1000, String.format("%03d", elapsedMilis % 1000), client.toString());
			} else {
				logger.debug("request task stopped by disconnect, {}", client.toString());
			}
			
		
			
		} catch (Exception e) {
			
			try {
				client.getServerContext().requestHandler().onExceptionDoRequest(client, request, e);
				
			} catch (Exception e2) {
				logger.error("occurred requestHandler's exceptionCaughtRequestExecute.. client disconnect", e2);
				client.disconnect();
			}
			
		} finally {
			
			
			request.release();
			client.releaseWriterLock();
		}
	}
}


