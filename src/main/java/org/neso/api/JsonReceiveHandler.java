package org.neso.api;

import org.neso.core.request.Client;
import org.neso.core.request.HeadBodyRequest;
import org.neso.core.request.HeadRequest;
import org.neso.core.request.handler.AbstractRequestHandler;
import org.neso.core.server.Server;

public class JsonReceiveHandler extends AbstractRequestHandler {
	
	//Dao dao;
	
	@Override
	public int headLength() {
		return 2;
	}
	
	@Override
	public int bodyLength(HeadRequest request) {
		return Integer.parseInt(new String(request.getHeadBytes()));
	}
	
	@Override
	public void doRequest(Client client, HeadBodyRequest request) throws Exception {
		
		String jsonString = new String(request.getBodyBytes());
		
		System.out.println("jsonString -> " + jsonString);
		//dao.save(jsonString);
		
		client.getWriter().write("OK".getBytes()).close();
	}
	
	
	public static void main(String[] args) {
		
		new Server(new JsonReceiveHandler(), 10010)
		.connectionOriented(false)	//비연결형, 한번의 요청, 한번의 응답
		.readTimeoutMillis(1000)
		.maxConnections(1000)
		.inoutLogging(true)
		.start();
	}
}
