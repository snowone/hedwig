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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.user.Alias;
import com.hs.mail.imap.user.User;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * Mailet that apply aliasing and forwarding
 * 
 * @author Won Chul Doh
 * @since Jun 24, 2010
 * 
 */
public class AliasingForwarding extends AbstractMailet {
	
	static Logger logger = Logger.getLogger(AliasingForwarding.class);

	public boolean accept(Set<Recipient> recipients, SmtpMessage message) {
		return CollectionUtils.isNotEmpty(recipients);
	}

	public void service(Set<Recipient> recipients, SmtpMessage message)
			throws MessagingException {
		List<Recipient> newRecipients = new ArrayList<Recipient>();
		List<Recipient> errors = new ArrayList<Recipient>();
		for (Iterator<Recipient> it = recipients.iterator(); it.hasNext();) {
			Recipient rcpt = it.next();
			User user = getUserManager().getUserByAddress(rcpt.getMailbox());
			if (user != null) {
				if (StringUtils.isNotEmpty(user.getForwardTo())) {
					// Forwarding takes precedence over local aliases
					try {
						// TODO optionally remove the original recipient
						it.remove();
						Recipient forwardTo = new Recipient(user.getForwardTo(), false);
						if (Config.isLocal(forwardTo.getHost())) {
							long id = getUserManager().getUserID(forwardTo.getMailbox());
							if (id != 0) {
								forwardTo.setID(id);
								newRecipients.add(forwardTo);
							} else {
								throw new AddressException(
										"Forwarding address not found.");
							}
						} else {
							message.setNode(SmtpMessage.ALL);
							message.addRecipient(forwardTo);
						}
					} catch (Exception e) {
						// Forwarding address is invalid or not found.
						StringBuffer errorBuffer =
							new StringBuffer(128)
								.append("Failed to forwarding ")
								.append(rcpt.getMailbox())
								.append(" to ")
								.append(user.getForwardTo());
						logger.error(errorBuffer.toString());
						errors.add(rcpt);
					}
				} else {
					rcpt.setID(user.getID());
				}
			} else {
				// Try to find aliases
				List<Alias> expanded = getUserManager().expandAlias(rcpt.getMailbox());
				it.remove();
				if (CollectionUtils.isNotEmpty(expanded)) {
					for (Alias alias : expanded) {
						newRecipients.add(new Recipient((Long) alias
								.getDeliverTo(), (String) alias.getUserID(),
								false));
					}
				} else {
					String errorMessage = new StringBuffer(64)
							.append(rcpt.getMailbox())
							.append("\r\n")
							.append("The mailbox specified in the address does not exist.")
							.toString();
					logger.error(new StringBuffer(128)
							.append("Permanent exception delivering mail (")
							.append(message.getName())
							.append("): ")
							.append(errorMessage)
							.append("\r\n").toString());
					errors.add(rcpt);
					message.appendErrorMessage(errorMessage);
				}
			}
		}

		if (newRecipients.size() > 0) {
			recipients.addAll(newRecipients);
		}
	}

}
