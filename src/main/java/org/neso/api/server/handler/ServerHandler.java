package org.neso.api.server.handler;
 
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.neso.api.Api;
import org.neso.core.exception.ApiNotFoundException;
import org.neso.core.exception.ClientAbortException;
import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.Session;
import org.neso.core.request.handler.AbstractRequestHandler;
import org.neso.core.server.internal.ByteBasedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Api 개념을 적용한 requestHandler
 * 
 * 
 */
public abstract class ServerHandler extends AbstractRequestHandler {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static String MATCH_API_ATTR_NAME = "_matched_api_obj";
	
	private Map<String, Api> apiHandlerMap = new ConcurrentHashMap<String, Api>();


	private Api matchingApi(HeadBodyRequest request) {
		
		String apiKey = apiKey(request);
		if (StringUtils.isEmpty(apiKey)) {
			return null;
		}

		for(Map.Entry<String, Api> api : apiHandlerMap.entrySet()) {
			if (StringUtils.equals(api.getKey().trim(), apiKey.trim())) {
				return api.getValue();
			}
		}
		return null;
    }
	
	public void registApi(String apiKey, Class<? extends Api> apiClass) {
		try {
			registApi(apiKey, apiClass.newInstance());
		} catch (InstantiationException iste) {
			throw new RuntimeException(iste);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		}
	}
	
	public void registApi(String apiKey, Api api) {
		apiHandlerMap.put(apiKey, api);
        logger.debug("{}({}) Api added to [{}]", apiKey, api.getClass().getSimpleName(), this.getClass().getSimpleName());
	}


	@Override
	final public void doRequest(Client client, HeadBodyRequest request) throws Exception {

		if (client.isConnected()) {
			
			Api matchedApi = null;
			try {
				matchedApi = matchingApi(request);
				if (matchedApi == null) {
					throw new ApiNotFoundException(request, null);
				}
				
				request.addAttribute(MATCH_API_ATTR_NAME, matchedApi);
			} catch (Exception e) {
				throw new ApiNotFoundException(request, e);
			}

			byte[] response = preApiExecute(client, request);
				
			if (response == null) {
				response = matchedApi.handle(request);

				byte[] postR = postApiExecute(client, request, response);
				if (postR != null) {
					response = postR;
				}
			}
			
			try {
				ByteBasedWriter writer = client.getWriter();
				writer.write(response == null ? new byte[0] : response);
				writer.close();
				
			} catch (Exception e) {
				onExceptionWrite(client, e);
			}
		} else {
			onExceptionWrite(client, new ClientAbortException(client));
		}
	}

	@Override
	final public void onExceptionRead(Client client, Throwable t) {
		
		
		logger.debug(client.toString() + " An error occurred while read : ", t);
		if (client.isConnected()) {
			byte[] errorMessage = null;
			try {
				errorMessage = exceptionCaughtRequestIO(client, t);

			} catch (Exception e) {
				logger.error("occurred serverHandler's exceptionCaughtRequestIO ");
			}
			if (errorMessage == null) {
				errorMessage = "read error".getBytes();
			}

			try {
				ByteBasedWriter writer = client.getWriter();
				writer.write(errorMessage);
				writer.close();

			} catch (Exception e) {
				logger.error("occurred serverHandler's write ");
			}

			
			client.disconnect();
		}
	}
	
	@Override
	final public void onExceptionWrite(Client client, Throwable exception) {
		logger.debug(client.toString() + " An error occurred while white : ", exception);
		if (client.isConnected()) {
			 
			try {
				exceptionCaughtRequestIO(client, exception);
				
			} catch (Exception e) {
				logger.error("occurred serverHandler's exceptionCaughtRequestIO ");
			}
			client.disconnect();
		}
	}
	
	@Override
	final public void onExceptionDoRequest(Client client, HeadBodyRequest request, Throwable exception) {
		
		logger.debug(client.toString() + " An error occurred while do request  :", exception);
		
		byte[] errorMessage = null;
		try {
			errorMessage = exceptionCaughtDoRequest(client, request, exception);
		} catch (Exception e) {
			logger.error("occurred serverHandler's exceptionCaughtRequestIO ");
		}
		if (errorMessage == null) {
			 errorMessage = "server error".getBytes();
		}
		
		ByteBasedWriter writer = client.getWriter();
		writer.write(errorMessage);
		writer.close();
	}
	

	
	protected abstract String apiKey(HeadBodyRequest request);
	
	protected abstract byte[] preApiExecute(Session session, HeadBodyRequest request);
    
	protected abstract byte[] postApiExecute(Session session, HeadBodyRequest request, byte[] response);
	
	protected abstract byte[] exceptionCaughtRequestIO(Session session, Throwable exception);
	
	protected abstract byte[] exceptionCaughtDoRequest(Session session, HeadBodyRequest request, Throwable exception);
}
