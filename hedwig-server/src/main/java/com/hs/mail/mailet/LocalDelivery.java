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
package com.hs.mail.mailet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.hs.mail.container.config.Config;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * Receives a Mail from SmtpMessageConsumer and takes care of delivery of the
 * message to local inboxes.
 * 
 * @author Won Chul Doh
 * @since Jun 7, 2010
 * 
 */
public class LocalDelivery extends AbstractMailet {

	static Logger logger = Logger.getLogger(LocalDelivery.class);

	private List<Mailet> mailets;
	
	public void setMailets(List<Mailet> mailets) {
		this.mailets = mailets;
	}
	
	public void init(MailetContext context) {
		super.init(context);
		for (Mailet aMailet : mailets) {
			aMailet.init(context);
		}
	}

	public boolean accept(Set<Recipient> recipients, SmtpMessage message) {
		return message.getNode() == SmtpMessage.LOCAL
				|| message.getNode() == SmtpMessage.ALL;
	}

	public void service(Set<Recipient> recipients, SmtpMessage message)
			throws MessagingException {
		Set<Recipient> temp = new HashSet<Recipient>();
		for (Recipient recipient : recipients) {
			if (Config.isLocal(recipient.getHost())) {
				temp.add(recipient);
			}
		}
		for (Mailet aMailet : mailets) {
			try {
				if (aMailet.accept(temp, message)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Processing " + message.getName()
								+ " through " + aMailet.getClass().getName());
					}
					aMailet.service(temp, message);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
