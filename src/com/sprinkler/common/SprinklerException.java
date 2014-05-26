package com.sprinkler.common;

public class SprinklerException extends RuntimeException {
    
    private static final long serialVersionUID = -5386355008323770858L;
    
    public SprinklerException(Throwable cause) {
        
        super(cause);
    }
    
    public SprinklerException(String message) {
        
        super(message);
    }
}
