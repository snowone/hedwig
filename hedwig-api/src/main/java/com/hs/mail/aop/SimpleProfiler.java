package com.hs.mail.aop;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

public class SimpleProfiler {

	protected Logger logger = null;

	protected String logName = null;

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}
	
	public Object profile(ProceedingJoinPoint call) throws Throwable {
		if (logger == null) {
			synchronized (this) {
				logger = Logger.getLogger((logName != null) ? logName
						: getClass().getName());
			}
		}
		StopWatch clock = new StopWatch(call.toShortString());
		clock.setKeepTaskList(false);
		clock.start();
		try {
			return call.proceed();
		} finally {
			clock.stop();
			logger.info(shortSummary(call, clock));
		}
	}
	
	private String shortSummary(ProceedingJoinPoint call, StopWatch clock) {
		return call.toShortString() + ": running time (millis) = "
				+ clock.getTotalTimeMillis() + " {"
				+ StringUtils.arrayToCommaDelimitedString(call.getArgs()) + "}";
	}
	
}
