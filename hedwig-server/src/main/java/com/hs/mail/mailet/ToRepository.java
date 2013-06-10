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

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Collection;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.message.Header;
import org.apache.log4j.Logger;

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.imap.message.MailMessage;
import com.hs.mail.imap.processor.fetch.BodyStructureBuilder;
import com.hs.mail.imap.processor.fetch.EnvelopeBuilder;
import com.hs.mail.sieve.Sieve;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * Mailet that actually stores the message
 * 
 * @author Won Chul Doh
 * @since 29 Jun, 2010
 * 
 */
public class ToRepository extends AbstractMailet {

	static Logger logger = Logger.getLogger(ToRepository.class);
	
	private BodyStructureBuilder builder = null;
	
	public ToRepository() {
		super();
		this.builder = new BodyStructureBuilder(new EnvelopeBuilder());
	}

	public boolean accept(Set<Recipient> recipients, SmtpMessage message) {
		return CollectionUtils.isNotEmpty(recipients);
	}
	
	public void service(Set<Recipient> recipients, SmtpMessage message)
			throws MessagingException {
		try {
			deliver(recipients, message);
		} catch (IOException e) {
		}
	}

	private void deliver(Collection<Recipient> recipients, SmtpMessage message)
			throws IOException {
		String returnPath = (message.getNode() != SmtpMessage.LOCAL) 
				? "Return-Path: <" + message.getFrom().getMailbox() + ">\r\n" 
				: null;
		MailMessage msg = message.getMailMessage();
		try {
			if (returnPath != null) {
				Header header = msg.getHeader().getHeader();
				header.setField(AbstractField.parse(returnPath));
				msg.setSize(msg.getSize() + returnPath.getBytes().length);
			}
			for (Recipient rcpt : recipients) {
				try {
					if (rcpt.getID() != -1) {
						if (!Sieve.runSieve(context, rcpt, message)) {
							context.storeMail(rcpt.getID(),
									ImapConstants.INBOX_NAME, message);
						}
					}
				} catch (Exception e) {
					StringBuilder errorBuffer = new StringBuilder(256)
							.append("Error while delivering message to ")
							.append(rcpt);
					logger.error(errorBuffer.toString(), e);

					if (!message.isNotificationMessage()) {
						errorBuffer.append(": ")
								.append(e.getMessage().trim())
								.append("\r\n");
						message.appendErrorMessage(errorBuffer.toString());
					}
				}
			}
		} catch (MimeException e) {
			// impossible really
		}
		if (msg != null && msg.getPhysMessageID() != 0) {
			try {
				if (returnPath != null) {
					PushbackInputStream is = new PushbackInputStream(msg
							.getInputStream(), returnPath.length());
					is.unread(returnPath.getBytes("ASCII"));
					msg.save(is);
				} else {
					msg.save(true);
				}
				builder.build(msg.getInternalDate(), msg.getPhysMessageID());
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (MimeException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
