package com.sprinkler.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sprinkler.common.SprinklerException;

public class Sprinkler {
    
    private static Sprinkler _instance = new Sprinkler();
    
    private final ConcurrentMap<Integer, SprinklerData> _data =
            new ConcurrentHashMap<Integer, SprinklerData>();
    
    private Sprinkler() {}
    
    public static Sprinkler getInstance() {
        
        return _instance;
    }
    
    public void reset() {
        
        _data.clear();
    }
    
    /**
     * Locks the calling thread until someone will release it, or timeout will occur.
     * 
     * @return data sent by releaser
     */
    public Object await(int key, long timeout) {
        
        SprinklerData data = null;
        try {
            data = getData(key);
            doAwait(data.getLatch(), timeout);
            externalizeException(data);
        } finally {
            _data.remove(key);
        }
        
        return data != null ? data.getInternal() : null;
    }
    
    /**
     * Releases the lock on the waiting thread(s) for the given key
     */
    public void release(int key) {
        
        release(key, null, null);
    }
    
    public synchronized void release(int key, Object internalData) {
        
        release(key, internalData, null);
    }
    
    public synchronized void release(int key, Throwable ex) {
        
        release(key, null, ex);
    }
    
    /**
     * Releases the lock on the waiting thread(s) for the given key, notifies them about the given
     * exception.
     */
    public synchronized void release(int key, Object internalData, Throwable ex) {
        
        SprinklerData data = getData(key);
        data.setInternal(internalData);
        data.setAlreadyReleased(true);
        if (ex != null) {
            data.setException(ex);
        }
        notify(data.getLatch());
    }
    
    private synchronized SprinklerData getData(int key) {
        
        SprinklerData data = _data.get(key);
        if (data == null) {
            data = new SprinklerData();
            _data.put(key, data);
        }
        
        return data;
    }
    
    private void externalizeException(SprinklerData data) {
        
        if (!isAlreadyReleased(data)) {
            throw new SprinklerException(new TimeoutException());
        }
        Throwable thrown = data.getException();
        if (thrown != null) {
            throw new SprinklerException(thrown);
        }
    }
    
    private void doAwait(CountDownLatch latch, long timeout) {
        
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new SprinklerException(ex);
        }
    }
    
    private synchronized boolean isAlreadyReleased(SprinklerData data) {
        
        return data.isAlreadyReleased();
    }
    
    private void notify(CountDownLatch lock) {
        
        lock.countDown();
    }
    
    private static class SprinklerData {
        
        private final CountDownLatch _latch;
        private boolean _isAlreadyReleased = false;
        private Throwable _thrown;
        private Object _internal;
        
        public SprinklerData() {
            
            _latch = new CountDownLatch(1);
        }
        
        public Object getInternal() {
            
            return _internal;
        }
        
        public void setInternal(Object data) {
            
            _internal = data;
        }
        
        public CountDownLatch getLatch() {
            
            return _latch;
        }
        
        public boolean isAlreadyReleased() {
            
            return _isAlreadyReleased;
        }
        
        public void setAlreadyReleased(boolean isAlreadyReleased) {
            
            _isAlreadyReleased = isAlreadyReleased;
        }
        
        public Throwable getException() {
            
            return _thrown;
        }
        
        public void setException(Throwable thrown) {
            
            _thrown = thrown;
        }
        
        @Override
        public String toString() {
            
            return String.format(
                    "SprinklerData [latch.count=%s, isAlreadyReleased=%s, internal=%s, thrown.message=%s]",
                    _latch == null ? "null latch" : _latch.getCount(),
                    _isAlreadyReleased,
                    _internal,
                    _thrown == null ? "null" : _thrown.getMessage());
        }
    }
}
