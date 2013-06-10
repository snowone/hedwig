package com.hs.mail.container.server.thread.sjt.mgmt;

/** An interface for a work group collection. Used to accomodate switching from
 * various types of collection classes. for example a linked list backed collection in a scalable 
 * system expecting massive work loads to an array backed on relying on faster insertions.
 * Uses Objects instead of Runnable in order to accomodate using complex work objects which may
 * include timestamps, priorities, etc.
 */

public interface WorkGroup {
    
    /** Add work to the wrapped collection.
     * @param work A simple of complex work object containing payload, timestamps, priority.
     */
    public void addWork(Object work);
    
    /** Remove work from the wrapped collection.The collection itself will decide whether this is
     * FIFO, LIFO, and any other necessary ordering.
     * @return work of type Object.
     */
    public Object removeWork();
    
    /** Check how many items in work collection.
     * @return size of work collection.
     */
    public int size();
    
    /** Check whether work has one or more work items in collection.
     * @return true or false.
     */
    public boolean isEmpty();
}
