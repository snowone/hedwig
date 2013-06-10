/*
 * Copyright 2010 the original author or authors.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hs.mail.util;

import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * 
 * @author Won Chul Doh
 * @since Dec 12, 2005
 *
 */
public class ObjectLocker {

    public static final long DEFAULT_MAX_WAIT = 3L * 1000L;
    public static final long DEFAULT_MIN_EVICTABLE_TIME_MILLIS = 5L * 60L * 1000L;
    public static final int DEFAULT_QUEUE_SIZE = 100;
	
 	static TreeSet _locks = new TreeSet();

    public ObjectLocker() {
        super();
    }
    
    protected long _maxWait = DEFAULT_MAX_WAIT;
    protected long _minEvictableTimeMillis = DEFAULT_MIN_EVICTABLE_TIME_MILLIS;
    protected int _queueSize = DEFAULT_QUEUE_SIZE;

 	public void lock(Object o) throws Exception {
        long starttime = System.currentTimeMillis();
        for (;;) {
            if (_locks.contains(o)) {
                synchronized (this) {
	                try {
	                    wait(_maxWait);
	                } catch (InterruptedException e) {
	                    // ignored
	                }
                }
                if ((System.currentTimeMillis() - starttime) >= _maxWait) {
                    throw new NoSuchElementException("Timeout waiting for idle object " + o);
                } else {
                    continue; // keep looping
                }
            } else {
                synchronized (this) {
                    _locks.add(o);
                }
                return;
            }
        }
    }
    
    public void unlock(Object o) {
        synchronized (this) {
            _locks.remove(o);
            if (_locks.size() >= _queueSize) {
                evict();
            }
            notifyAll();
        }
    }
    
    private void evict() {
    }
    
}
