package com.hs.mail.container.server.thread.sjt.util;

import com.hs.mail.container.server.thread.sjt.mgmt.ThreadingGroup;

/** Gate gathers all running thread and synch them once a particular task is executed.In
 * the wait method, threads simply wait until the last thread is gathered. In the join method
 *  threads wait until each exit their run method.
 */

public class Gate {
    
    private int size;
    private int counter;
    
    /** Create a Gate with a thread size.
     * @param size number of threads to wait
     */
    public Gate(int size) {
        this.size = size;
        this.counter = size;
    }
    
    /** Place a thread into the wait state, and notify once the last thread size calls method.
     * @throws InterruptedException
     */
    public synchronized void synch() throws InterruptedException {
        //decrement counter
        counter--;
        
        // if this is the last thread to call synch then notifyall
        if ( counter <= 0) {
            notifyAll();
            return;
        }
        
        //otherwise wait
        while ( counter > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                notifyAll();
            }
        }
    }
    
    
    /** Perform an immediate interrupt on the gate, which frees all waiting threads.
     */
    public synchronized void interrupt() {
        notifyAll();
    }
    
    
    /** Perform a join on all threads in the java.lang.ThreadGroup.
     * @param group group of threads.
     */
    public static void join(ThreadGroup group) {
        Thread[] list = new Thread[group.activeCount() ];
        group.enumerate(list) ;
        join(list);
    }
    
    /** Perform a join on all threads in the Thread array.
     * @param list collection of threads.
     */
    public static void join(Thread[] list) {
        
        for (int i =0; i < list.length; i++) {
            Thread thread = list[i];
            
            if (thread != null) {
                if (thread.isAlive() )
                    thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException at Gate.join()");
                }
            }
        }
    }
    
    
    /** Perform a join on all threads in the ThreadingGroup.
     * @param group ThreadingGroup collection of running threads.
     */
    public static void wait(ThreadingGroup group) {
        
        if (group != null) {
            for (int i=0; i <group.size(); i++) {
                Thread thread = group.useThread();
                if (thread != null) {
                    
                    //		    if (thread.isAlive() )
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
    
    /** Perform an immediate interrupt and join on all threads in the ThreadingGroup.
     * @param group ThreadingGroup collection of running threads.
     */
    public static void join(ThreadingGroup group) {
        
        for (int i =0; i < group.size(); i++) {
            Thread thread = group.useThread();
            
            if (thread != null) {
                if (thread.isAlive() )
                    thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException at Gate.join()");
                }
            }
        }
    }
    
}




