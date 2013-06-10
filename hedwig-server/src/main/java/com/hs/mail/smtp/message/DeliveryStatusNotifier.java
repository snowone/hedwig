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
package com.hs.mail.smtp.message;

import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.hs.mail.container.config.Config;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 17, 2010
 *
 */
public class DeliveryStatusNotifier {
	
	private static String SORRY_FOR_DELIVERY_ERROR = "I'm afraid I wasn't able to deliver your message to the following addresses.\r\n"
			+ "This is a permanent error; I've given up.\r\n"
			+ "Below I included the list of recipients and the reason why I was unable to deliver your message.\r\n\r\n";
	
	public static void dsnNotify(String from, MailAddress to, MimeMessage msg,
			String textMessage) {
		// To prevent loops in error reporting, we specify reverse-path to null.
		SmtpMessage dsnmsg = new SmtpMessage(new MailAddress());
		dsnmsg.addRecipient(new Recipient(to.getMailbox(), false));
		try {
			dsnmsg.setContent(createMimeMessage(from, to, msg, textMessage));
			dsnmsg.store();
			dsnmsg.createTrigger();
		} catch (Exception e) {
			dsnmsg.dispose();
		}
	}
	
	private static MimeMessage createMimeMessage(String from, MailAddress to,
			MimeMessage msg, String textMessage) throws MessagingException {
		Session session = Session.getInstance(System.getProperties(), null);
		MimeMessage dsn = new MimeMessage(session);
		dsn.setFrom(new InternetAddress((from != null) ? from : Config
				.getPostmaster()));
		InternetAddress[] toAddr = { to.toInternetAddress() };
		dsn.setRecipients(Message.RecipientType.TO, toAddr);
		String subject = msg.getHeader("Subject", null);
		if (subject != null) {
			if (from != null && !subject.regionMatches(true, 0, "Re: ", 0, 4))
				dsn.setHeader("Subject", "Re: " + subject);
			else if (from == null
					&& !subject.regionMatches(true, 0, "[Err] ", 0, 6))
				dsn.setHeader("Subject", "[Err] " + subject);
		}
		dsn.setSentDate(new Date());
		Multipart mp = new MultipartReport((from != null) ? textMessage
				: SORRY_FOR_DELIVERY_ERROR + textMessage, msg);
		dsn.setContent(mp);
		return dsn;
	}

	public static class MultipartReport extends MimeMultipart {
		public MultipartReport(String text, MimeMessage msg)
				throws MessagingException {
			super("report");
			ContentType ct = new ContentType(contentType);
			ct.setParameter("report-type", "delivery-status");
			contentType = ct.toString();
			MimeBodyPart mbp = new MimeBodyPart();
			mbp.setText(text);
			addBodyPart(mbp);
			// TODO delivery-status part must be included.
			if (msg != null) {
				// Attach the original message.
				mbp = new MimeBodyPart();
				mbp.setContent(msg, "message/rfc822");
				addBodyPart(mbp);
			}
		}
	}

}
