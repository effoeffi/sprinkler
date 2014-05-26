package com.sprinkler.concurrency;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolJoiner extends ThreadPoolExecutor {
    
    Collection<Future<?>> _tasks = new CopyOnWriteArrayList<Future<?>>();
    
    public ThreadPoolJoiner() {
        
        super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }
    
    public void join() throws InterruptedException, ExecutionException {
        
        for (Future<?> currTask : _tasks) {
            currTask.get();
        }
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        
        Future<T> ret = super.submit(task);
        _tasks.add(ret);
        
        return ret;
    }
}