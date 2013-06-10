package com.hs.mail.container.server.thread.sjt.mgmt;

/** An interface for a threading group collection. Used to accomodate switching from
 * various types of collection classes. May simply wrap a java.util.ThreadGroup,
 * or may simply use an array of threads.
 */

public interface ThreadingGroup   {
    
    /** Add thread to the wrapped collection. Threads are added by ThreadManager only.
     * @param thread One working thread.
     */
    public void addThread(Thread thread);
    
    /** Remove one thread from the wrapped collection. The collection itself will decide which
     * thread is returned. FIFO, LIFO, and any other necessary ordering. Thread should be added
     * back into the collection by the ThreadManager.
     * @return thread A working thread.
     */
    public Thread removeThread();
    
    public boolean removeThread(Thread thread);
    
    /** Similiar to removeThread but the thread reference is maintained in Group.
     * @return thread A working thread.
     */
    public Thread useThread();
    
    /** Check how many items in work collection.
     * @return size of work collection.
     */
    public int size();

    /** Check last index item in work collection.
     * @return pointer of work collection.
     */
    public int pointer();
    
    /** Check whether work has one or more work items in collection.
     * @return true or false if collection empty.
     */
    public boolean isEmpty();

    public Thread getThread(int index);
    
    /** Destroy can be called by the user code in order to prompt joining and cleanup of all 
     * working threads. Destroy is also called by the finalize method, during garbage collection, 
     * if the user code does not call it explicitly.
     */
    public void destroy();
}
