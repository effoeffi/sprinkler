package com.sprinkler.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.Test;

import com.sprinkler.common.ExecutorServiceFactory;
import com.sprinkler.common.SprinklerException;
import com.sprinkler.concurrency.Sprinkler;

public class TestSprinkler {
    
    @Test
    public void testReleaseBeforeAwait() throws InterruptedException {
        
        Sprinkler.getInstance().release(1);
        long beforeAwait = getCurrentTime();
        Sprinkler.getInstance().await(1, 100000);
        Assert.assertTrue((getCurrentTime() - beforeAwait) < 200);
    }
    
    @Test
    public void testAwait_ReleaserDeliversData() {
        
        final int CONTEXT = 1;
        final String DATA = "bla bla";
        
        ExecutorServiceFactory.getCachedThreadPoolExecutor().submit(new Callable<Void>() {
            
            @Override
            public Void call() throws Exception {
                
                Sprinkler.getInstance().release(CONTEXT, DATA);
                
                return null;
            }
            
        });
        String data = (String) Sprinkler.getInstance().await(CONTEXT, 10000);
        Assert.assertEquals(DATA, data);
    }
    
    @Test
    public void testAwait_InnerThreadExternalizeException() {
        
        final int CONTEXT = 1;
        final String EXCEPTION_MESSAGE = "test inner thread exception message";
        
        ExecutorServiceFactory.getCachedThreadPoolExecutor().submit(new Callable<Void>() {
            
            @Override
            public Void call() throws Exception {
                
                Sprinkler.getInstance().release(CONTEXT, new RuntimeException(EXCEPTION_MESSAGE));
                
                return null;
            }
            
        });
        
        Throwable thrown = null;
        try {
            Sprinkler.getInstance().await(CONTEXT, 10000);
        } catch (Throwable t) {
            thrown = t;
        }
        Assert.assertTrue(thrown instanceof SprinklerException);
        Assert.assertEquals(EXCEPTION_MESSAGE, thrown.getCause().getMessage());
    }
    
    @Test
    public void testAwait_Timeout() {
        
        final int CONTEXT = 1;
        
        ExecutorServiceFactory.getCachedThreadPoolExecutor().submit(new Callable<Void>() {
            
            @Override
            public Void call() throws Exception {
                
                Thread.sleep(10000);
                
                return null;
            }
            
        });
        
        Throwable thrown = null;
        try {
            Sprinkler.getInstance().await(CONTEXT, 1);
        } catch (Throwable t) {
            thrown = t;
        }
        Assert.assertTrue(thrown.getCause() instanceof TimeoutException);
    }
    
    private long getCurrentTime() {
        
        return System.currentTimeMillis();
    }
}
