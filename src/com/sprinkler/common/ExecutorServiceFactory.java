package com.sprinkler.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceFactory {
    
    private static ExecutorService _executor = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
    
    private ExecutorServiceFactory() {}
    
    public static ExecutorService getCachedThreadPoolExecutor() {
        
        return _executor;
    }
}
