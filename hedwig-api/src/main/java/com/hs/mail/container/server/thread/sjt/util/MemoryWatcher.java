package com.hs.mail.container.server.thread.sjt.util;

/** The MemoryWatcher provides some measure of memory protection,
 * specifically helping to prevent the  * @see java.lang.OutOfMemoryException.
 */

public class MemoryWatcher extends Thread {
    
    final Condition condition;
    final float capacity;
    boolean isEmergency;
    
    public MemoryWatcher(Condition condition, float capacity) {
        super("MW");
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        this.condition = condition;
        this.capacity = capacity;
        isEmergency = false;
    }
    
    public float freeMemory() {
        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        
        System.out.println("Memory total:"+total+"free:"+free);
        
        return (float)(total - free)/(float)total;
    }
    
    public boolean isEmergency() {
        return isEmergency;
    }
    
    
    public void run() {
        while(true) {
            try {
                
                while (freeMemory() >= capacity) {
                    
                    isEmergency = true;
                    
                    System.out.println("Encouraging GC");
                    Runtime.getRuntime().gc();
                    
                    if (freeMemory() >= capacity)
                        this.sleep(100);
                    else {
                        //isEmergency = false;
                        //notify of free memory solution
                        //			    try {
                        condition.broadcast();
                        //} 
                        break;
                    }
                }
                this.sleep(500);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
