package com.hs.mail.container.server.thread.sjt.util;

/**
 * Condition resembles the POSIX conditional variable. User threads can signal and wait in a manner 
 * similar to notify and wait provided by java.lang.Object
 * <br>
 * Following is the closest similarity in behaviour of Object and Condition.
 *<ul>
 * <li>object.wait() = condition.untimed_wait()
 * <li>object.wait(100) = condition.timed_wait(100)
 * <li>object.notify() = condition.signal()
 * <li>object.notifyAll() = condition.broadcast()
 * </ul>
 * Note: Condition is an Object -- Use the correct notification method, don't use the Object wait and notify
 */

public class Condition {
    
    private Flag flag;
    private String name;

    public String toString() {
        return name;
    }
    
    /** Create a Condition.
     */
    public Condition() {
        this(new Flag("temp") );
    }
    
    /** Create a Condition with a Flag.
     */
    public Condition(Flag flag) {
        this.flag = flag;
    }
    
    public Condition(Flag flag, String name) {
        this(flag);
        this.name = "sjt.util.Condition["+name+"]";
    }
    
    /** Wait forever.
     * @throws InterruptedException
     * @see java.lang.Object#wait
     */
    public void untimed_wait() throws InterruptedException {
        timed_wait(0);
    }
    
    /** Wait for specified number of milliseconds.
     * @param millis number of millisecond to wait.
     * @throws IllegalMonitorStateException if thread is not owner.
     * @throws InterruptedException if thread is interrupted.
     */
    public void timed_wait(int millis) throws InterruptedException  {
        timed_wait(flag, millis);
    }
    
    /** Wait for specified number of milliseconds. 
     * @throws IllegalMonitorStateException if thread is not owner.
     * @throws InterruptedException if thread is interrupted
     */
    public void timed_wait(Flag flag, int millis) throws InterruptedException {
        int i = 0;
        InterruptedException exception = null;
        
        synchronized(this) {
            
            if ( flag.owner() != Thread.currentThread() ) {
                throw new IllegalMonitorStateException("Thread not owner");
            }
            
            //release flag in order to allow other thread access
            while (flag.owner() == Thread.currentThread() ) {
                i++;
                flag.release();
            }
            
            //wait for however long. wait(0) equiv to wait()
            try {
                if (millis == 0) 
                    wait();
                else
                    wait(millis);
            } catch (InterruptedException e) {
                //		System.out.println(this+" held by "+Thread.currentThread()+" interrupted during wait, will acquire");
                exception = e;
            }
        }
        
        //for loop is needed for nesting
        for (; i > 0; i--) {
            flag.acquire();
        }
        //if thread interrupted then throw the error now
        if (exception != null)
            throw exception;
        
        return;
    }
    
    /** Send signal to one threads waiting on Condition.
     * @throws IllegalMonitorStateException if thread is not owner.
     */
    public void signal() {
        signal(flag);
    }
    
    /** Send Signal to one thread waiting on Condition.
     * @param flag 
     * @throws IllegalMonitorStateException if thread is not owner.
     */
    public synchronized void signal(Flag flag) {
        if (flag.owner() != Thread.currentThread() )
            throw new IllegalMonitorStateException("Thread not owner");
        
        notify();
    }
    
    /** Send singal to all threads waiting on Condition.
     * @throws IllegalMonitorStateException if thread is not owner.
     */
    public void broadcast() {
        broadcast(flag);
    }
    
    /** Send signal to all threads waiting on Condition.
     * @throws IllegalMonitorStateException if thread is not owner.
     */
    public synchronized void broadcast(Flag flag) {
        if (flag.owner() != Thread.currentThread() )
            throw new IllegalMonitorStateException("Thread not owner");
        
        notifyAll();
    }
    
}
