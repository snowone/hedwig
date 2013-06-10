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
package com.hs.mail.sieve;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.jsieve.mail.ActionFileInto;
import org.apache.jsieve.mail.ActionKeep;
import org.apache.jsieve.mail.ActionRedirect;
import org.apache.jsieve.mail.ActionReject;

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.mailet.MailetContext;
import com.hs.mail.smtp.message.DeliveryStatusNotifier;
import com.hs.mail.smtp.message.MailAddress;

public class Actions {

    /**
     * Constructor for Actions.
     */
	private Actions() {
		super();
	}

    /**
     * <p>
     * Executes the passed ActionFileInto.
     * </p>
     * 
     * <p>
     * This implementation accepts any destination with the root of <code>INBOX</code>.
     * </p>
     * 
     * <p>
     * As the current POP3 server does not support sub-folders, the mail is
     * stored in the INBOX for the recipient of the mail and the full intended
     * destination added as a prefix to the message's subject.
     * </p>
     * 
     * <p>
     * When IMAP support is added to James, it will be possible to support
     * sub-folders of <code>INBOX</code> fully.
     * </p>
     * 
     * @param anAction
     * @param aMail
     * @param aMailetContext
     * @throws MessagingException
     */
	public static void execute(ActionFileInto anAction, SieveMailAdapter aMail,
			MailetContext aMailetContext) throws MessagingException {
		boolean delivered = false;
		try {
			String destination = anAction.getDestination();
			aMailetContext.storeMail(aMail.getSoleRecipientID(), destination,
					aMail.getMessage());
			delivered = true;
		} catch (IOException e) {
			throw new MessagingException(e.getMessage(), e);
		} finally {
			// Ensure the mail is always ghosted
		}
		if (delivered) {
		}
	}

    /**
     * <p>
     * Executes the passed ActionKeep.
     * </p>
     * 
     * <p>
     * In this implementation, "keep" is equivalent to "fileinto" with a
     * destination of "INBOX".
     * </p>
     * 
     * @param anAction
     * @param aMail
     * @param aMailetContext
     * @throws MessagingException
     */
	public static void execute(ActionKeep anAction, SieveMailAdapter aMail,
			MailetContext aMailetContext) throws MessagingException {
		ActionFileInto action = new ActionFileInto(ImapConstants.INBOX_NAME);
		execute(action, aMail, aMailetContext);
	}

    /**
     * Method execute executes the passed ActionRedirect.
     * 
     * @param anAction
     * @param aMail
     * @param aMailetContext
     * @throws MessagingException
     */
	public static void execute(ActionRedirect anAction, SieveMailAdapter aMail,
			MailetContext aMailetContext) throws MessagingException {
		try {
			detectAndHandleLocalLooping(aMail, anAction.getAddress());
			aMailetContext.sendMail(aMail.getSoleRecipient(),
					new String[] { anAction.getAddress() }, aMail.getMessage());
		} catch (IOException e) {
			throw new MessagingException(e.getMessage(), e);
		}
	}

    /**
     * <p>
     * Method execute executes the passed ActionReject. It sends an RFC 2098
     * compliant reject MDN back to the sender.
     * </p>
     * 
     * @param anAction
     * @param aMail
     * @param aMailetContext
     * @throws MessagingException
     */
	public static void execute(ActionReject anAction, SieveMailAdapter aMail,
			MailetContext aMailetContext) throws MessagingException {
		detectAndHandleLocalLooping(aMail, null);
		// Create the MDN part
		StringBuilder humanText = new StringBuilder(128);
		humanText.append("This message was refused by the recipient's mail filtering program.");
		humanText.append("\r\n");
		humanText.append("The reason given was:");
		humanText.append("\r\n");
		humanText.append("\r\n");
		humanText.append(anAction.getMessage());
		
		MimeMessage message = aMail.getMessage().getMimeMessage();
		DeliveryStatusNotifier.dsnNotify(aMail.getSoleRecipient(),
				new MailAddress(message.getReplyTo()[0]), message,
				humanText.toString());
	}

	/**
	 * Detect and handle locally looping mail. External loop detection is left
	 * to the MTA.
	 * 
	 * @param aMail
	 * @param aMailetContext
	 * @param anAttributeSuffix
	 * @throws MessagingException
	 */
	protected static void detectAndHandleLocalLooping(SieveMailAdapter aMail,
			String recipient) throws MessagingException {
		if (aMail.getMessage().isNotificationMessage()) {
			// Don't reject or redirect notification message.
			throw new MessagingException(
					"This message is a notification message!");
		}
		try {
			String from = getSender(aMail);
			if (from != null && from.equals(recipient)) {
				MessagingException ex = new MessagingException(
						"This message is looping!");
				throw ex;
			}
		} catch (IOException e) {
			throw new MessagingException(e.getMessage(), e);
		}
	}
	
	private static String getSender(SieveMailAdapter aMail) throws IOException {
		return aMail.getMessage().getMailMessage().getFrom();
	}

}
