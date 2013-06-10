package com.hs.mail.container.server.thread.sjt.mgmt;

import java.util.Iterator;
import org.apache.log4j.Priority;

/**
 * Class to provide thread monitoring
 */
public class SimpleThreadIterator implements Iterator {

    static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(SimpleThreadIterator.class);

    private ThreadingGroup threads = null;
    private int baseThreadCount = 0;
    private ThreadingGroup aiThreads = null;
    private int curIndex = 0;

    public SimpleThreadIterator(ThreadingGroup threads, ThreadingGroup aiThreads) {
        this.threads = threads;
        this.baseThreadCount = this.threads.size();
        this.aiThreads = aiThreads;
    }

    public boolean hasNext() {
        return (this.curIndex < (this.baseThreadCount + this.aiThreads.size()));
    }

    public Object next() {
        Thread t = null;

        try {
            if (this.curIndex < this.baseThreadCount) {
                t = this.threads.getThread(this.curIndex);
            } else {
                t = this.aiThreads.getThread(this.curIndex - this.baseThreadCount);
            }
        } catch (Exception e) {
            // If the auto-incremented thread has been destroyed, an exception can occur.
            // We ignore this exception.
        } finally {
            ++this.curIndex;
        }

        return t;
    }

    public void remove() {
    }
}