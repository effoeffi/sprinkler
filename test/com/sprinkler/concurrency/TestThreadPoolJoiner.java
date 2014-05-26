package com.sprinkler.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;

public class TestThreadPoolJoiner {
    
    @Test
    public void testJoin() throws InterruptedException, ExecutionException {
        
        final int TASKS = 10;
        final AtomicInteger executedTasks = new AtomicInteger(0);
        ThreadPoolJoiner joiner = new ThreadPoolJoiner();
        for (int i = 0; i < TASKS; i++) {
            joiner.submit(new Callable<Integer>() {
                
                @Override
                public Integer call() {
                    
                    return executedTasks.incrementAndGet();
                }
            });
        }
        joiner.join();
        Assert.assertEquals(TASKS, executedTasks.get());
    }
}
