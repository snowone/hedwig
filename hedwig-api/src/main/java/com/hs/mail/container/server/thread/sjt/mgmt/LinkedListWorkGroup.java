package com.hs.mail.container.server.thread.sjt.mgmt;

import java.util.List;
import java.util.LinkedList;

/** This is implementation of the WorkGroup interface. In this case, the implementation is 
 * based on a java.util.LinkedList. As a FIFO type structure, the work is added to the bottom 
 * of the list, and removed from the top.
 */

public class LinkedListWorkGroup implements WorkGroup {
    
    private LinkedList         list;
    
    public LinkedListWorkGroup() {
        list = new LinkedList();
    }
    
    /** Add work to the wrapped collection.
     * @param work A simple of complex work object containing payload, timestamps, priority.
     */
    public void addWork(Object work) {
        list.add(work);
    }
    
    /** Remove work from the wrapped collection.The collection itself will decide whether this is
     * FIFO, LIFO, and any other necessary ordering.
     * @return work of type Object.
     */
    public Object removeWork() {
        return list.removeFirst();
    }
    
    /** Check how many items in work collection.
     * @return size of work collection.
     */
    public int size() {
        return list.size();
    }
    
    /** Check whether work has one or more work items in collection.
     * @return true or false.
     */
    public boolean isEmpty() {
        return ( list.size() == 0 ? true: false);
    }    
}
