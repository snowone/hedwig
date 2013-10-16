/*
 * Copyright 2010 the original author or authors.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hs.mail.container.simple;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

/**
 * Simple spring component container
 * 
 * @author Won Chul Doh
 * @since Jan 11, 2010
 */
public class SimpleSpringContainer {

	private static Logger logger = Logger.getLogger(SimpleSpringContainer.class);
	
	private static final String IMPL_CLASS_NAME = "org.springframework.context.support.FileSystemXmlApplicationContext";

	private static final String DEFAULT_CONFIG_LOCATION = "../conf/applicationContext.xml";
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private static final Options OPTS = new Options();
	static {
		OPTS.addOption(OptionBuilder.withArgName("file")
				.hasArg()
				.withDescription("Configuration file path")
				.create("c"));
		OPTS.addOption(OptionBuilder.withArgName("interval")
				.hasOptionalArg()
				.withDescription("Memory usage dump interval")
				.create("dm"));
	}
	
	protected String[] configLocations;
	
	protected Object applicationContext;

	public SimpleSpringContainer(String[] configLocations) {
		super();
		this.configLocations = configLocations;
	}

 	public Object createFileSystemXmlApplicationContext(String[] configLocations)
			throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class klass = classLoader.loadClass(IMPL_CLASS_NAME);

		Object[] args = new Object[] { configLocations, Boolean.FALSE };
		Object applicationContext = ConstructorUtils.invokeConstructor(klass, args);
		MethodUtils.invokeMethod(applicationContext, "refresh", null);

		return applicationContext;
	}

	public void start() throws Exception {
		this.applicationContext = createFileSystemXmlApplicationContext(this.configLocations);
		SpringContainerShutdownHook hook = new SpringContainerShutdownHook(this);
		Runtime.getRuntime().addShutdownHook(hook);
	}

    public void forceShutdown() {
		try {
			MethodUtils.invokeMethod(this.applicationContext, "stop", null);
			MethodUtils.invokeMethod(this.applicationContext, "close", null);
		} catch (Exception e) {
		}
	}
    
    /**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine cli = null;
		try {
			cli = new PosixParser().parse(OPTS, args);
		} catch (ParseException e) {
			usage();
			System.exit(0);
		}

		try {
			String configLocation = cli.getOptionValue("c", DEFAULT_CONFIG_LOCATION);
			System.setProperty("app.home", new File(configLocation)
					.getParentFile().getParent());
			SimpleSpringContainer container = new SimpleSpringContainer(
					new String[] { configLocation });
			container.start();

			if (cli.hasOption("dm")) {
				startPerformanceMonitor(cli.getOptionValue("dm", "5"));
			}
		} catch (Exception e) {
			String errMsg = (e.getMessage() != null) ? e.getMessage() : e.getCause().getMessage();
			System.err.println(errMsg);
			logger.fatal(errMsg, e);
		}
	}
	
	private static void startPerformanceMonitor(String interval) {
		int period = Integer.parseInt(interval);
		Timer timer = new Timer();
		TimerTask memoryTask = new TimerTask() {
			ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
			MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

			@Override
			public void run() {
				int threadCount = threadBean.getThreadCount();
				int peakThreadCount = threadBean.getPeakThreadCount();
				MemoryUsage heap = memoryBean.getHeapMemoryUsage();
				MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
				System.out.println(sdf.format(System.currentTimeMillis())
						+ "\t threads=" + threadCount + ";peakThreads="
						+ peakThreadCount + ";heap=" + heap + ";non-heap="
						+ nonHeap);
			}
		};
		timer.schedule(memoryTask, 0, period * 1000);
	}

	private static void usage() {
		HelpFormatter hf = new HelpFormatter();
		String runProgram = "java " + SimpleSpringContainer.class.getName()
				+ " [options]";
		hf.printHelp(runProgram, OPTS);
	}
	
}

final class SpringContainerShutdownHook extends Thread {
	private SimpleSpringContainer container;

	protected SpringContainerShutdownHook(SimpleSpringContainer container) {
		this.container = container;
	}

	public void run() {
		this.container.forceShutdown();
	}
}
