package com.hs.mail.container.server.thread.sjt.mgmt;

import java.util.List;
import java.util.LinkedList;

/** An implementation of ThreadingGroup interface. In this case, the implementation is 
 * based on a simple thread linked list
 * The ThreadList is under strict control of the ThreadManager class, which adds, removes
 * and uses threads accordingly.
 */
public class ThreadList implements ThreadingGroup {
    
    private LinkedList threads;
    private volatile int pointer;
    private volatile int size;
    
    /** Construct a thread collection using a thread name and collection size.
     * @param name assigned to threads.
     * @param size size of collection.
     */
    public ThreadList(String name, int size) {
        this.size = size;
        threads = new LinkedList();
    }
    
    /** Add thread to the wrapped collection. Threads are added by ThreadManager only.
     * @param thread One working thread.
     */
    public synchronized void addThread(Thread thread) {
        threads.add(thread);
        ++pointer;
    }
    
    /** Remove one thread from the wrapped collection. The collection itself will decide which
     * thread is returned. FIFO, LIFO, and any other necessary ordering. Thread should be added
     * back into the collection by the ThreadManager.
     * @return thread A working thread.
     */	
    public Thread removeThread() {
        --pointer;
        return (Thread) threads.removeLast();
    }

    public synchronized boolean removeThread(Thread thread) {
        --pointer;
        return threads.remove(thread);
    }
    
    /** Similiar to removeThread but the thread reference is maintained in Group.
     * @return thread A working thread.
     */
    public Thread useThread() {
        --pointer;
        return (Thread) threads.getLast();
    }
    
    /** Check how many items in work collection.
     * @return size of work collection.
     */
    public int size() {
        return size;
    }

    public Thread getThread(int index) {
        return (Thread) this.threads.get(index);
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
