package org.neso.core.exception;

import java.util.Arrays;

import org.neso.core.request.HeadBodyRequest;


@SuppressWarnings("serial")
public class ApiNotFoundException extends RuntimeException {

    private HeadBodyRequest headRequest;
    
    public ApiNotFoundException(HeadBodyRequest request, Throwable th) {
        super("can't found api," + Arrays.toString(request.getAllBytes()), th);
        this.headRequest = request;
    }
    
 
    
    public HeadBodyRequest getHeadBodyRequest() {
    	return this.headRequest;
    }
}
