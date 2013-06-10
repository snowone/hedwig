package com.hs.mail.container.server.thread.sjt.mgmt;

import java.util.Iterator;

import com.hs.mail.container.server.thread.sjt.util.Condition;
import com.hs.mail.container.server.thread.sjt.util.Flag;

/** 
 * SimpleWorkManager is the newest implementation of the SJT.Mgmt threading solutions. By
 * throttling the work list to a predetermined capacity, this WorkManager provides additional memory protection,
 * specifically helping to prevent subclasses of java.lang.VirtualMachineError, most likely the java.lang.OutOfMemoryError<br>
 * <br>
 * The work group size is prevented from growing beyond the capacity, and additional work is either blocked or 
 * refused. addBlockingWork will block until the size is less than capacity. addWork will throw an exception and not service
 * the work. Refusing work may seem contrary to proper application procedure; however, it prevents a complete 
 * application melt-down. Throttling adheres to the  premise that servicing most (or some) requests is better than servicing none.
 *
 * <pre>
 *   WorkManager manager;
 *   
 *   manager = new SimpleWorkManager("workmgr", 2, 10000, WorkManager.BLOCK);
 *       ...
 *   manager.addWork( 
 *                  new Runnable() { 
 *                      public void run() { 
 *                                        System.out.println("I am running");
 *                      } }
 *                  );
 *   
 * </pre>
 */

public class SimpleWorkManager implements WorkManager {

	final WorkGroup workList;
	final ThreadManager threadManager;

	final Condition isReady, isEmpty, isAble, isComplete;

	final Flag workGroupFlag;

	volatile boolean isActive;
	final Condition isEmergency;

	final int capacity;
	final int emergencyMode;

	final int minthreadsize;
	final int maxthreadsize;
	final boolean autoIncrementalMode;

	/**
	 * Construct with default parameters: name of 'sjt.mgmt', one working
	 * thread, and no throttling.
	 * 
	 * @param name
	 *            identifier for thread manager, also passed to all working
	 *            threads.
	 */
	public SimpleWorkManager() {
		this("sjt.mgmt");
	}

	/**
	 * Construct with default parameters of one working thread, and no
	 * throttling.
	 * 
	 * @param name
	 *            identifier for thread manager, also passed to all working
	 *            threads.
	 */
	public SimpleWorkManager(String name) {
		this(name, 1);
	}

	/**
	 * Construct with default of no throttling.
	 * 
	 * @param name
	 *            identifier for thread manager, also passed to all working
	 *            threads.
	 * @param size
	 *            size of the working threads pool.
	 */
	public SimpleWorkManager(String name, int size) {
		this(name, size, -1);
	}

	/**
	 * Construct with default for the throttling mode; blocking.
	 * 
	 * @param name
	 *            identifier for thread manager, also passed to all working
	 *            threads.
	 * @param size
	 *            size of the working threads pool.
	 * @param capacity
	 *            number of items in work group prior to attaining
	 *            emergencyMode. -1 indicated no emergency.
	 */
	public SimpleWorkManager(String name, int size, int capacity) {
		this(name, size, capacity, WorkManager.BLOCK);
	}

	/**
	 * Creates new SimpleWorkManager, construct an empty WorkGroup.
	 * 
	 * @param name
	 *            identifier for thread manager, also passed to all working
	 *            threads.
	 * @param size
	 *            size of the working threads pool.
	 * @param capacity
	 *            number of items in work group prior to attaining
	 *            emergencyMode. -1 indicated no emergency.
	 * @param emergencyMode
	 *            instructions once the capacity has been reached.
	 */
	public SimpleWorkManager(String name, int size, int capacity,
			int emergencyMode) {
		this(name, size, size, 0, capacity, emergencyMode);
	}

	/**
	 * Creates new SimpleWorkManager, construct an empty WorkGroup.
	 * 
	 * @param name
	 *            identifier for thread manager, also passed to all working
	 *            threads.
	 * @param minSize
	 *            minimum size of the working threads pool.
	 * @param maxSize
	 *            maximum size of the working threads pool.
	 * @param capacity
	 *            number of items in work group prior to attaining
	 *            emergencyMode. -1 indicated no emergency.
	 * @param emergencyMode
	 *            instructions once the capacity has been reached.
	 */
	public SimpleWorkManager(String name, int minSize, int maxSize,
			int maxIdleTime, int capacity, int emergencyMode) {
		this.capacity = (capacity >= 2 || capacity == -1) ? capacity : 100;
		this.minthreadsize = (minSize >= 1) ? minSize : 1;
		this.maxthreadsize = Math.max(minthreadsize, (maxSize >= 1) ? maxSize
				: 1);
		this.autoIncrementalMode = (this.maxthreadsize > this.minthreadsize);

		this.emergencyMode = emergencyMode;

		workList = new LinkedListWorkGroup();
		workGroupFlag = new Flag("workgroup");

		isReady = new Condition(workGroupFlag, "isReady");
		isEmpty = new Condition(workGroupFlag, "isEmpty");
		isAble = new Condition(workGroupFlag, "isAble");
		isComplete = new Condition(workGroupFlag, "isComplete");

		isEmergency = new Condition();

		// construct the thread mgr
		threadManager = new SimpleThreadManager(name, this, minthreadsize,
				maxthreadsize, maxIdleTime);
		// flag for accepting work
		isActive = true;
	}

	/**
	 * Add a Runnable object to the work list, and notify waiting threads of new
	 * work.
	 * 
	 * @param work
	 *            The Runnable instance containing executable code.
	 * @throws IllegalStateException
	 *             if not accepting new work.
	 */
	public void addWork(Runnable work) {
		this.addWork(work, null);
	}

	/**
	 * Add a Runnable object to the work list, and notify waiting threads of new
	 * work.
	 * 
	 * @param work
	 *            The Runnable instance containing executable code.
	 * @throws IllegalStateException
	 *             if inactive or not accepting new work.
	 */
	public void addWork(Runnable work, Object lock) {
		// check to see if still accepting work
		if (!isActive)
			throw new IllegalStateException("WorkManager not active");

		// check for null first.
		if (work == null)
			return;

		try {
			// wait here until lock is acquired
			workGroupFlag.acquire();

			// this is throttling code. loop here until size is less than
			// capacity.
			if (capacity != -1) {
				int workListSize = workList.size();

				while (workListSize >= capacity) {
					switch (emergencyMode) {
					case (WorkManager.REJECT):
						throw new IllegalStateException(
								"WorkManager has reached capacity. Refused work");

					case (WorkManager.BLOCK):
						try {
							// change to isCapacity
							isReady.untimed_wait();
						} catch (InterruptedException e) {
							throw new IllegalStateException(
									"WorkManager was interrupted");
						}
						break;

					default:
						throw new IllegalStateException(
								"WorkManager has reached capacity. Refused work.");
					}
				} // end while capacity...

				if (this.autoIncrementalMode) {
					if (workListSize > this.minthreadsize) {
						threadManager.increment(workListSize
								- this.minthreadsize);
					}
				}
			} else {
				if (this.autoIncrementalMode) {
					int workListSize = workList.size();

					if (workListSize > this.minthreadsize) {
						threadManager.increment(workListSize
								- this.minthreadsize);
					}
				}
			}

			// build an array with work and lock
			Object[] contract = { work, lock };
			workList.addWork(contract);
			// signal threads that objects have been added to list
			isReady.signal();

		} catch (InterruptedException e) {
			throw new IllegalStateException("WorkManager interrupted");

		} finally {
			workGroupFlag.release();
		}
	}

	/**
	 * Add a Runnable object to the work list, notify waiting threads of new
	 * work, and block until run method exits
	 * 
	 * @param work
	 *            The Runnable instance containing executable code.
	 * @throws IllegalStateException
	 *             if not accepting new work.
	 * @throws InterruptedException
	 *             if thread is interrupted.
	 */
	public void addBlockingWork(Runnable work) throws InterruptedException,
			IllegalStateException {
		this.addBlockingWork(work, new byte[0]);
	}

	/**
	 * Add a Runnable object to the work list, notify waiting threads of new
	 * work, and block on provided lock object until run method exits.
	 * 
	 * @param work
	 *            The Runnable instance containing executable code.
	 * @throws IllegalStateException
	 *             if not accepting new work.
	 * @throws InterruptedException
	 *             if thread is interrupted.
	 */
	public void addBlockingWork(Runnable work, Object lock)
			throws InterruptedException, IllegalStateException {
		synchronized (lock) {
			this.addWork(work, lock);
			lock.wait();
		}
	}

	/**
	 * Set thread resources to null once work list is null.
	 */
	public void destroy() {
		// make sure all tasks have been completed, and halt all new tasks
		if (workList != null && isActive == true) {
			isActive = false;

			try {
				// acquire will wait until free to acquire the flag
				workGroupFlag.acquire();

				// loop until work list is empty
				while (workList.size() > 0) {
					try { // isEmpty
						isComplete.timed_wait(200);
					} catch (InterruptedException e) {
						System.out.println(Thread.currentThread().getName()
								+ " Manager Interrupted while destroying.");
					}
				}

			} catch (InterruptedException e) {
				return;
			} finally {
				workGroupFlag.release();
			}

			// once work list is empty, destroy the working threads
			if (threadManager != null)
				threadManager.destroy();

		}
	}

	public Iterator threadIterator() {
		return threadManager.threadIterator();
	}

	public int threadCount() {
		return threadManager.threadCount();
	}

	/**
	 * Override finalize method with one that calls destroy.
	 */
	protected void finalize() {
		destroy();
	}

}
