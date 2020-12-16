package org.neso.api.server.handler;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public abstract class AbstractServerHandler extends ServerHandler {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected AbstractServerHandler() {
		checkConcreteGetApi(this.getClass());
	}

    private void checkConcreteGetApi(Class<? extends AbstractServerHandler> c) {

        Class<?> clazz = c;
        boolean isConcreteGetApiFromHead = false;
        boolean isConcreteGetApiFromBody = false;
        boolean isConcreteGetBodyLength = false;
        while (!clazz.equals(AbstractServerHandler.class)) {
            Method[] thisMethods = clazz.getDeclaredMethods();
            for (Method method : thisMethods ) {
            	if ("apiKeyFromHead".equals(method.getName())) {
            		isConcreteGetApiFromHead = true;
            	}
            	
            	if ("apiKeyFromBody".equals(method.getName())) {
            		isConcreteGetApiFromBody = true;
            	}
            	
            	if ("bodyLength".equals(method.getName())) {
            		isConcreteGetBodyLength = true;
            	}
            }
            clazz = clazz.getSuperclass();
        }
        
        if (!isConcreteGetApiFromHead && !isConcreteGetApiFromBody) {
        	logger.error("required override 'apiIdFromHead' or 'apiIdFromBody' in serverHandler ->  {} ", this.getClass().getSimpleName());
        	//throw new RuntimeException("required override 'getApiIdFromHead' or 'getApiIdFromBody' in ServerHandler");
        }
        
        if (!isConcreteGetBodyLength) {
        	logger.error("required override 'getBodyLength' in serverHandler ->  {} ", this.getClass().getSimpleName());
        }
    }



	
	protected String apiKeyFromHead(byte[] head) {
		return StringUtils.EMPTY;
	}
	
	protected String apiKeyFromBody(byte[] body) {
		return StringUtils.EMPTY;
	}
	
}