package com.hs.mail.container.server.thread.sjt.mgmt;

/** An implementation of ThreadingGroup interface. In this case, the implementation is 
 * based on a simple thread array [].
 * The ThreadArray is under strict control of the ThreadManager class, which adds, removes
 * and uses threads accordingly.
 */

public class ThreadArray implements ThreadingGroup {
    
    private Thread[] threads;
    private volatile int pointer;
    private volatile int size;
    
    /** Construct a thread collection using a thread name and collection size.
     * @param name assigned to threads.
     * @param size size of collection.
     */
    public ThreadArray(String name, int size) {
        this.size = size;
        threads = new Thread[size];
    }
    
    /** Add thread to the wrapped collection. Threads are added by ThreadManager only.
     * @param thread One working thread.
     */
    public void addThread(Thread thread) {
        threads[pointer++] = thread;
    }
    
    /** Remove one thread from the wrapped collection. The collection itself will decide which
     * thread is returned. FIFO, LIFO, and any other necessary ordering. Thread should be added
     * back into the collection by the ThreadManager.
     * @return thread A working thread.
     */	
    public Thread removeThread() {
        Thread thread = threads[--pointer];
        threads[pointer] = null;
        return thread;
    }

    public boolean removeThread(Thread thread) {
        boolean removed = false;

        for (int i = pointer; i >= 0; i--) {
            try {
                if (thread == threads[i]) {
                    threads[i] = null;
                    --pointer;
                    removed = true;
                    break;
                }
            } catch (Exception e) {
            }
        }

        return removed;
    }
    
    /** Similiar to removeThread but the thread reference is maintained in Group.
     * @return thread A working thread.
     */
    public Thread useThread() {
        return (Thread)threads[--pointer];
    }
    
    /** Check how many items in work collection.
     * @return size of work collection.
     */
    public int size() {
        return size;
    }

    public Thread getThread(int index) {
        return this.threads[index];
    }

    /** Check last index item in work collection.
     * @return pointer of work collection.
     */
    public int pointer() {
        return pointer;
    }
    
    /** Check whether work has one or more work items in collection.
     * @return true or false if collection empty.
     */
    public boolean isEmpty() {
        return (pointer == 0 ? true : false);
    }
    
    /** Destroy can be called by the user code in order to prompt joining and cleanup of all 
     * working threads. Destroy is also called by the finalize method, during garbage collection, 
     * if the user code does not call it explicitly.
     */
    public void destroy() {
        
    }
    
}
