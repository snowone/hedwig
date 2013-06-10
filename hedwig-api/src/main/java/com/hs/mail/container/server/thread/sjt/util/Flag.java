package com.hs.mail.container.server.thread.sjt.util;

/** 
 * Flag is a token object with  behaviour similar to the lock associated with each java.lang.Object.
 * This flag can be nested, ensuring that recursive calls to the same Flag are held by the same owner.
 * The internal counter will increment for each recursive call to acquire, and decrement for 
 * each recursive call to release.
 * 
 * NOTE: Always code the release method within a finally clause.
 * <pre>
 *   try {
 *       flag.acquire();
 *       ...
 *   }
 *   finally {
 *       flag.release();
 *   }
 * </pre>
 */

public class Flag {
    
    String name;
    
    public Flag(String name) {
        this.name = "com.hs.frmwk.server.thread.sjt.util.Flag["+name+"]";
    }
    
    //owner of flag
    protected Thread flag = null;
    
    //counter accomodates nesting, each acquire must have matching release
    protected int count = 0;
    
    /**  Acquire the flag, or wait until it is available.
     * @throws InterruptedException if thread is interrupted during acquire.
     */
    public synchronized void acquire() throws InterruptedException {
        
        while (is_owner() == false) {
            //try catch was commented out...
            try {
                wait(100); //ideally no looping. wait() should be sufficient!
            } catch (InterruptedException e) {
                System.out.println(flag+" is interrupted during acquire");
                throw e;
            }
        }
    }
    
    
    /** Is the calling thread the owner of this Flag.
     * @return true or false
     */
    private synchronized boolean is_owner() {
        if (flag == null) {
            flag = Thread.currentThread();
            count = 1;
            return true;
        }
        
        if (flag == Thread.currentThread() ) {
            count++;
            return true;
        }
        
        return false;
    }
    
    /** Thread will release the Flag.
     */
    public synchronized void release() {
        if (flag == Thread.currentThread() ) {
            count--;
            //if nesting is complete, then set count to 0 and flag to null
            //free lock
            if (count == 0) {
                flag = null;
                notify();
            }
            
        }
    }
    
    /** Return the Thread owning this Flag.
     * @return thread
     */
    public synchronized Thread owner() {
        return flag;
    }
    
    /** Overloads the toString method.
     * @return name of flag
     */
    public String toString() {
        return name;
    }

}