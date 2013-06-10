package com.hs.mail.container.server.thread.sjt.mgmt;

/** WorkManager is the public interface for all SJT.Mgmt threading solutions. It is expected 
 * that other parties may wish to implement better or different solutions other than the ones
 * provided with this library.
 */

import java.util.Iterator;

public interface WorkManager {
    
    public static final int        BLOCK         = 1;
    public static final int        REJECT        = 2;
    
    /** The primary interface to all SJT.Mgmt threading solutions.
     * @param work The Runnable instance containing executable code.
     * @throws IllegalStateException if not accepting new work.
     */
    public abstract void addWork(Runnable work);
    
    /** The Object lock is used by the thread manager to notify the callers code.
     * @param work The Runnable instance containing executable code.
     * @param lock The object which is used as a wait, notify lock. if null then 
     *             the wait notify is useless.
     * @throws IllegalStateException if not accepting new work.
     */
    public abstract void addWork(Runnable work, Object lock);
    
    
    /** In contrast with addWork methods, addBlockingWork adds a wait condition
     * so that the execution halts once the work is added to the thread manager.
     * @param work The instance containing executable code.
     */
    public abstract void addBlockingWork(Runnable work) throws InterruptedException ;
    
    /** The Object lock will be used by the thread manager to notify the callers code.
     * @param work The Rnnable instance containing executable code.
     * @param lock The object which is used as a wait, notify lock. if null then 
     *             the wait notify is useless.
     */
    public abstract void addBlockingWork(Runnable work, Object lock) throws InterruptedException;
    
    /** Destroy can be called by the user code in order to prompt joining and cleanup of all 
     * working threads. Destroy is also called by the finalize method, during garbage collection, 
     * if the user code does not call it explicitly.
     */
    public abstract void destroy();

    public abstract Iterator threadIterator();
    public abstract int threadCount();
}
