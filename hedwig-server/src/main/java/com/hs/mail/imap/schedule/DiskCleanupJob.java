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
package com.hs.mail.imap.schedule;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.mailbox.MailboxManager;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 28, 2010
 *
 */
public class DiskCleanupJob extends QuartzJobBean {

	static Logger logger = Logger.getLogger(DiskCleanupJob.class);
	
	private MailboxManager manager;

	public void setMailboxManager(MailboxManager manager) {
		this.manager = manager;
	}

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting disk cleanup job.");
		}
		String prop = Config.getProperty("stop_cron_after", "2h");
		Date stopAt = ScheduleUtils.getTimeAfter(prop, DateUtils.addHours(
				new Date(), 2));
		if ((prop = Config.getProperty("expunge_after", null)) != null) {
			new MessageExpunger(manager).expunge(prop, stopAt.getTime());
		}
		if ((prop = Config.getProperty("compress_after", null)) != null) {
			new MessageCompressor().compress(prop, stopAt.getTime());
		}
	}
	
}
