package com.hs.mail.container.server.thread;

import java.util.ArrayList;
import java.util.Iterator;
import org.springframework.beans.factory.DisposableBean;

public class ManagedThread extends Thread {

    private long accessTime = 0L;
    private String accessDescription = null;
    private ArrayList disposables = null;

    public ManagedThread() {
        super();
    }

    public ManagedThread(Runnable job) {
        super(job);
    }

    public ManagedThread(String name) {
        super(name);
    }

    public ManagedThread(Runnable job, String name) {
        super(job, name);
    }

    public void setAccessDescription(String accessDescription) {
        this.accessTime = System.currentTimeMillis();
        this.accessDescription = accessDescription;
    }

    public String getAccessDescription() {
        return this.accessDescription;
    }

    public long getAccessTime() {
        return this.accessTime;
    }

    public void addDisposable(DisposableBean disposable) {
        if (null == this.disposables) {
            this.disposables = new ArrayList();
        }
        this.disposables.add(disposable);
    }

    public void releaseDisposables() {
        this.accessTime = 0L;
        this.accessDescription = null;

        if (null != this.disposables) {
            try {
                Iterator it = this.disposables.iterator();
                while (it.hasNext()) {
                    try {
                        DisposableBean disposable = (DisposableBean) it.next();
                        disposable.destroy();
                    } catch (Throwable th2) {
                    }
                }
            } catch (Throwable th1) {
            }

            try {
                this.disposables.clear();
            } catch (Throwable th) {
            }
        }
    }
}