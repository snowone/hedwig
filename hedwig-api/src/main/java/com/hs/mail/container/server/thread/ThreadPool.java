package com.hs.mail.container.server.thread;

import java.util.Iterator;

import org.springframework.core.task.TaskExecutor;

public interface ThreadPool extends TaskExecutor {

	public void invokeLater(Runnable task);

	public void execute(Runnable task);

	public Iterator threadIterator();

	public int threadCount();

}
