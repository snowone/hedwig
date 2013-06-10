package com.hs.mail.container.server.thread.sjt.mgmt;

import java.util.Iterator;

/** ThreadManager is the interface for SJT.Mgmt threading model implementations. It is expected 
 * that other parties may wish to implement better or different solutions other than the ones
 * provided with this library.
 */

public interface ThreadManager {
    
    /** Destroy can be called by the WorkManager in order to prompt joining and cleanup of all 
     * working threads. Destroy is also called by the finalize method, during garbage collection, 
     * if the user code does not call it explicitly.
     */
    public abstract void destroy();

    public void increment(int diff);
    public Iterator threadIterator();
    public int threadCount();
}
