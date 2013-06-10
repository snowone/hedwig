package com.hs.mail.container.server.thread;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.hs.mail.container.server.thread.sjt.mgmt.SimpleWorkManager;
import com.hs.mail.container.server.thread.sjt.mgmt.WorkManager;

public class SjtThreadPool implements ThreadPool, InitializingBean, DisposableBean {
	
	static Logger logger = Logger.getLogger(SjtThreadPool.class);
	
    private final static int DEFAULT_MAX_THREADS = 40;
    private final static int DEFAULT_MIN_THREADS = 40;
    private final static int MAX_IDLE_TIME = 10;
    private final static int DEFAULT_CAPACITY = 10000;

    private boolean disabled = false;
    private boolean stopped = false;
    private String name = "";
    private int maxThreads = DEFAULT_MAX_THREADS;
    private int minThreads = DEFAULT_MIN_THREADS;
    private int maxIdleTime = MAX_IDLE_TIME;
    private int capacity = DEFAULT_CAPACITY;
    private int emergencyMode = WorkManager.REJECT;

    private WorkManager manager = null;

	public SjtThreadPool() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(int minThreads) {
		this.minThreads = minThreads;
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getEmergencyMode() {
		return emergencyMode;
	}

	public void setEmergencyMode(String emergencyMode) {
		this.emergencyMode = "block".equals(emergencyMode) ? WorkManager.BLOCK
				: WorkManager.REJECT;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void afterPropertiesSet() throws Exception {
		if (logger.isDebugEnabled()) {
            logger.debug(getClass().getName() + 
                    ": Max#(" + this.maxThreads + "),Min#(" + this.minThreads +
                    "),MaxIdleTime(" + this.maxIdleTime + 
                    "),Capacity(" + this.capacity +
                    "),Emergency("
					        + ((this.emergencyMode == WorkManager.BLOCK) 
					        		? "block" : "reject") + ")");
		}
		
		initialize();
	}

    public void initialize() {
        if (this.disabled) {
            return;
        }
        
        try {
            this.manager = new SimpleWorkManager ( this.name, 
                                                   this.minThreads, this.maxThreads, 
                                                   this.maxIdleTime, this.capacity, 
                                                   this.emergencyMode );
            
            if (logger.isInfoEnabled()) {
                logger.info("Threadpool initialized: " + this);
            }
        } catch (Exception e) {
            logger.error("Cannot create SimpleWorkManager: " + e.getMessage());
        }
    }
	
	public void destroy() throws Exception {
        if (this.disabled) {
            return;
        }
        
        if (null != this.manager) {
            this.manager.destroy();
        }
	}

	public void invokeLater(Runnable task) {
        this.manager.addWork(task);
	}

	public void execute(Runnable task) {
        invokeLater(task);
	}

	public Iterator threadIterator() {
        return this.manager.threadIterator();
	}

	public int threadCount() {
        return this.manager.threadCount();
	}

}
