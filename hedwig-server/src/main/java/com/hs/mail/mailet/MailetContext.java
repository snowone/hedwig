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

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.smtp.message.SmtpMessage;

public interface MailetContext {

	/**
	 * 
	 * @return
	 */
	public MailboxManager getMailboxManager();

	/**
	 * 
	 * @return
	 */
	public UserManager getUserManager();

	/**
	 * Send an outgoing message to the top of this mailet container's root queue. 
	 * This is the equivalent of opening an SMTP session to localhost.
	 * 
	 * @param sender
	 *            the sender of the message
	 * @param recipients
	 *            an Array of mail addresses of recipients
	 * @param msg
	 *            the SmtpMessage of the headers and body content of the
	 *            outgoing message
	 * @throws IOException 
	 */
	void sendMail(String sender, String[] recipients, SmtpMessage msg) throws IOException;

	/**
	 * Stores the message in the target folder.
	 * 
	 * @param destination
	 *            the destination folder where this message will be stored
	 * @param msg
	 *            the SmtpMessage to store in a local mailbox
	 * @throws IOException 
	 */
	void storeMail(long soleRecipientID, String destination, SmtpMessage msg)
			throws IOException;

}
