package com.hs.mail.container.server.thread.sjt.mgmt;

import java.util.Iterator;
import com.hs.mail.container.server.thread.ManagedThread;
import com.hs.mail.container.server.thread.sjt.util.Gate;
import com.hs.mail.container.server.thread.sjt.util.Condition;

/**
 * SimpleThreadManager is in charge of construction, starting, and
 * destroying working threads. Each working threads pulls off work from the work list.
 */

public class SimpleThreadManager implements ThreadManager {

    private final ThreadingGroup        threads;
    private ThreadingGroup              aiThreads;
    private final SimpleWorkManager     manager;
    
    private final String                name;
    private final int                   minsize;
    private final int                   maxsize;
    private final int                   maxIdleTime;
    private final String                aiName;
    private final boolean               autoIncrementalMode;
    private final int                   aiTotalSize;
    private boolean                     isActive;
    
    private volatile int                activeCounter = 0;
    
    public SimpleThreadManager(String name, SimpleWorkManager manager, int maxsize) {
        this(name, manager, maxsize, maxsize, 0);
    }

    public SimpleThreadManager(String name, SimpleWorkManager manager, int minsize, int maxsize, int maxIdleTime) {
        this.name = name;
        this.aiName = name + ".ai";
        this.minsize = minsize;
        this.maxsize = maxsize;
        this.aiTotalSize = maxsize - minsize;
        this.autoIncrementalMode = (this.maxsize > this.minsize);
        this.maxIdleTime = maxIdleTime;
        this.manager = manager;
        
        //create a thread collection
        threads = new ThreadArray(name, minsize);
        for (int i=0; i < minsize; i++) {
            Thread thread = new WorkingThread(name + i);
            thread.start();
            threads.addThread( thread );
        }

        if (this.autoIncrementalMode) {
            aiThreads = new ThreadList(name + ".ai", this.aiTotalSize);
        }
        
        isActive = true;
    }

    public void increment(int diff) {
        int curSize = this.aiThreads.pointer() + 1;

        if ((curSize >= diff) || (curSize >= this.aiTotalSize)) {
            return;
        } else {
            Thread thread = new WorkingThread(aiName + curSize, true);
            thread.start();
            aiThreads.addThread( thread );
        }
    }
    
    /** Called by code or the finalize method.
     */
    public void destroy() {
        isActive = false;
        if (threads != null)
            Gate.join(threads);
        if (aiThreads != null)
            Gate.join(aiThreads);
    }

    public Iterator threadIterator() {
        return (new SimpleThreadIterator(threads, aiThreads));
    }

    public int threadCount() {
        return (threads.size() + aiThreads.size());
    }
    
    /** Performs thread cleanup. Called by the GC.
     */
    protected void finalize() {
        destroy();
    }
    
    /** WorkingThread pulls work of the work list,
     * and executes the run method of the Runnable work.
     */
    class WorkingThread extends ManagedThread {
        
        private final boolean temporary;
        /** Constructs a list with a ThreadGroup and Name.
         */
        public WorkingThread(String name) {
            this(name, false);
        }

        /** Constructs a list with a ThreadGroup and Name.
         */
        public WorkingThread(String name, boolean temporary) {
            super(name);
            this.temporary = temporary;
        }
        
        /** Pulls work off the work list and executes the work Runnable method.
         */
        public void run() {
            Object[] contract = null;
            
            while(!isInterrupted() ) {
                try {
                    //get global lock
                    manager.workGroupFlag.acquire();
                    //while(isRunnning && ..size)
                    while( manager.workList.size() <= 0 ) {
                        if (!temporary) {
                            try {
                                //wait for the isReady condition. DO NOT USE object.wait
                                manager.isReady.untimed_wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        } else {
                            try {
                                manager.isReady.timed_wait(maxIdleTime);
                            } catch (InterruptedException e) {
                                return;
                            }

                            if (manager.workList.size() <= 0) {
                                aiThreads.removeThread(this);
                                return;
                            }
                        }
                    }
                    
                    //check if thread is still supposed to run.
                    if (isInterrupted() ) {
                        return;
                    }		
                    //remove one task from the task list		
                    contract = (Object[])manager.workList.removeWork();
                    
                    //if work list is now empty then signal the empty condition
                    if (manager.workList.isEmpty() )
                        manager.isEmpty.signal();
                    
                    //new 1.1 signal that one object is removed, has space for more.
                    manager.isReady.signal();
                    
                    //end of synchronized block
                } catch (InterruptedException e) {
                    //only return if you have work
                    if (contract == null)
                        return;
                } finally {
                    //release the global lock
                    manager.workGroupFlag.release();
                }
                
                //the runnable work is array[0]
                Runnable oneTask = (Runnable)contract[0];
                
                //execute the runnable method within the runnable task
                oneTask.run();
                
                //notifyAll if lock not null
                if (contract[1] != null) {
                    synchronized( contract[1] ) {
                        contract[1].notifyAll();
                    }
                    //not using conditions ((Condition)contract[1]).signal();
                }
                
                //indicate that work group is empty and work is done.
                if (manager.workList.size() <= 0 ) {
                    try {
                        manager.workGroupFlag.acquire();
                        manager.isComplete.signal();
                    } catch (InterruptedException e) {
                    } finally {
                        //release the global lock
                        manager.workGroupFlag.release();
                    }
                }
                
                contract = null;
                
                
            }
            //	    System.out.println("Thread is exiting run method "+this);
        }
        
        public void destroy() {
            //this doesnt seem to make sense but it is ok
            //	    this.interrupt();
        }
        
        protected void finalize() {	    
            destroy();
        }
        
    }
    
}
