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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.search.ComparisonKey;
import com.hs.mail.imap.message.search.CompositeKey;
import com.hs.mail.imap.message.search.InternalDateKey;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * 
 * @author Won Chul Doh
 * @since Oct 2, 2010
 *
 */
public class MessageExpunger {
	
	static Logger logger = Logger.getLogger(MessageExpunger.class);
	
	private MailboxManager manager;

    public MessageExpunger(MailboxManager manager) {
		this.manager = manager;
	}
    
	public void expunge(String prop, long timeLimit) {
		Map<String, Date> criteria = getExpungeCriteria(prop);
		if (MapUtils.isNotEmpty(criteria)) {
			expungeMailboxes(criteria, timeLimit);
		}
	}
	
	private Map<String, Date> getExpungeCriteria(String prop) {
		String[] tokens = StringUtils.split(prop);
		int sz = tokens.length / 2;
		if (sz > 0) {
			Map<String, Date> criteria = new HashMap<String, Date>(sz);
			for (int i = 0; i < tokens.length / 2; i++) {
				Date base = ScheduleUtils.getDateBefore(tokens[i + 1]);
				if (base != null) {
					criteria.put(tokens[i], base);
				}
			}
			return criteria;
		}
		return null;
	}
	
	private void expungeMailboxes(Map<String, Date> criteria, long timeLimit) {
		for (String name : criteria.keySet()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Expunging messages from "
						+ name
						+ " which are older than "
						+ DateFormatUtils.ISO_DATE_FORMAT.format(criteria
								.get(name)) + ".");
			}
			String mbox = BASE64MailboxEncoder.encode(name);
			List<Long> mailboxIdes = manager.getMailboxIDList(mbox);
			if (CollectionUtils.isNotEmpty(mailboxIdes)) {
				for (Long mailboxID : mailboxIdes) {
					if (System.currentTimeMillis() >= timeLimit) {
						return;
					}
					expungeMessages(mailboxID, criteria.get(name));
				}
			}
		}
	}
	
	private void expungeMessages(long mailboxID, Date date) {
		List<Long> uids = manager.search(null, mailboxID, new CompositeKey(
				new InternalDateKey(ComparisonKey.LT, date)), null);
		if (CollectionUtils.isNotEmpty(uids)) {
			for (Long uid : uids) {
				manager.deleteMessage(uid);
			}
		}
	}
	
}
