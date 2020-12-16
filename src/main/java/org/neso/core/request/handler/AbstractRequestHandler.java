package org.neso.core.request.handler;


import java.nio.charset.Charset;

import org.neso.core.netty.ByteBasedWriter;
import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.handler.task.RequestTask;
import org.neso.core.request.internal.OperableHeadBodyRequest;
import org.neso.core.server.ServerContext;
import org.neso.core.support.RequestRejectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRequestHandler implements RequestHandler {

	private final static String DEFAULT_REJECT_MESSAGE = "server is too busy";

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Charset serverCharSet = Charset.defaultCharset();
	
	
	public void setCharset(Charset charSet) {
		this.serverCharSet = charSet;
	}
	
	@Override
	public Charset getCharset() {
		return serverCharSet;
	}


	@Override
	public void onRequest(Client client, HeadBodyRequest req) {
		
		if (req instanceof OperableHeadBodyRequest) {
			
			OperableHeadBodyRequest request = (OperableHeadBodyRequest) req;
			RequestTask task = new RequestTask(client, request);
 
			if (!client.getServerContext().requestExecutor().registerTask(task)) {
				logger.debug("request cant registered in the request pool, {}", client.toString());
				
				byte[] rejectMessage = DEFAULT_REJECT_MESSAGE.getBytes();
				if (client.getServerContext().requestHandler() instanceof RequestRejectListener) {
					
					RequestRejectListener listener = (RequestRejectListener) client.getServerContext().requestHandler();
				
					try {
						rejectMessage = listener.onRequestReject(client.getServerContext(), client.getServerContext().requestExecutor().getMaxRequets(), request);
					} catch (Exception e) {
						logger.error("occurred requestRejectListner's onRequestReject", e);
					}
				}
				
				ByteBasedWriter writer = client.getWriter();
				writer.write(rejectMessage);
				writer.close();
			} else {
				//logger.debug("request is registered in the request pool");
			}
	
		} else {
			throw new RuntimeException("not... request instanceof OperableHeadBodyRequest ...TODO ");
		}
	}
	
	@Override
	public void init(ServerContext context) {
		
	}

	@Override
	public void onConnect(Client client) {
		
	}

	@Override
	public void onDisconnect(Client client) {
		
	}
}
