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

import java.util.Set;

import javax.mail.MessagingException;

import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 7, 2010
 * 
 */
public interface Mailet {

	/**
	 * Initialize the mailet. Called by the SmtpMessageConsumer to indicate to a
	 * mailet that the mailet is being placed into service.
	 * <p>
	 * The SmtpMessageConsumer calls the init method exactly once after
	 * instantiating the mailet. The init method must complete successfully
	 * before the mailet can receive any requests.
	 */
	public void init(MailetContext context);

	/**
	 * Check if the mailet is interested in the given recipients and message. If
	 * this method returns false, the service method will not be called for this
	 * message.
	 * 
	 * @param recipients
	 *            collection of recipients this mailet must process
	 * @param message
	 *            the Mail object that contains the message and routing
	 *            information
	 * @return true if the mailet want to process the message, otherwise false
	 */
	public boolean accept(Set<Recipient> recipients, SmtpMessage message);

	/**
	 * Called by the SmtpMessageConsumer to allow the mailet to process the
	 * message.
	 * 
	 * @param recipients
	 *            collection of recipients this mailet must process
	 * @param message
	 *            the Mail object that contains the message and routing
	 *            information
	 * @throws MessagingException
	 */
	public void service(Set<Recipient> recipients, SmtpMessage message)
			throws MessagingException;

}
