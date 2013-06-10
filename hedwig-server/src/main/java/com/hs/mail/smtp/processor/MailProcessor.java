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
package com.hs.mail.smtp.processor;

import java.util.StringTokenizer;

import com.hs.mail.container.config.Config;
import com.hs.mail.container.server.socket.TcpTransport;
import com.hs.mail.smtp.SmtpException;
import com.hs.mail.smtp.SmtpSession;
import com.hs.mail.smtp.message.MailAddress;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * Handler for MAIL command. Starts a mail transaction to deliver mail as the
 * stated sender.
 * 
 * @author Won Chul Doh
 * @since May 29, 2010
 * 
 */
public class MailProcessor extends AbstractSmtpProcessor {

	@Override
	protected void doProcess(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws SmtpException {
		// In general, the MAIL command may be sent only when no mail
		// transaction is in progress.
		SmtpMessage message = session.getMessage();
		if (session.getClientDomain() == null || message != null) {
			throw new SmtpException(SmtpException.COMMAND_OUT_OF_SEQUENCE);
		}
		if (Config.isSaslAuthEnabled() && session.getAuthID() < 0) {
			throw new SmtpException(SmtpException.AUTH_REQUIRED);
		}
		if (st.countTokens() < 1) {
			throw new SmtpException(SmtpException.INVALID_COMMAND_PARAM);
		}
		String sender = nextToken(st);
		if (!startsWith(sender, "FROM:")) {
			throw new SmtpException(SmtpException.INVALID_COMMAND_PARAM);
		}
		if (sender.length() == 5) {
			if (!st.hasMoreTokens())
				throw new SmtpException(SmtpException.MISSING_SENDER_ADDRESS);
			sender = nextToken(st);
		} else {
			sender = sender.substring(5);
		}
		int lastChar = sender.indexOf('>', sender.indexOf('<'));
		if ((lastChar > 0) && (sender.length() > lastChar + 2)
				&& (sender.charAt(lastChar) + 1) == ' ') {
			String options = sender.substring(lastChar + 2);
			sender = sender.substring(0, lastChar + 2);
			
			StringTokenizer optSt = new StringTokenizer(options, " ");
			while (optSt.hasMoreElements()) {
				String option = optSt.nextToken();
				int i = option.indexOf('=');
				String key = option;
				String value = "";
				if (i > 0) {
					key = option.substring(0, i).toUpperCase();
					value = option.substring(i + 1);
				}
				if ("SIZE".equals(key)) {
					// Handle the SIZE extension keyword
					doMailSize(session, value);
				} else {
					
				}
			}
		}
		
		MailAddress from = null;
		if ("<>".equals(sender)) {
			// In the case of delivery notification message, the reverse-path is
			// set to null.
			from = new MailAddress();
		} else {
			from = new MailAddress(sender);
		}
		// Initiate a mail transaction
		session.createSmtpMessage(from);
		StringBuilder sb = new StringBuilder().append("250 ")
				.append("2.1.0")
				.append(" Sender <")
				.append(from.getMailbox())
				.append("> OK");
		session.writeResponse(sb.toString());
	}

	private void doMailSize(SmtpSession session, String value)
			throws SmtpException {
		int size = 0;
		try {
			size = Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			logger.error("Rejected syntactically incorrect value '" + value
					+ "' for SIZE parameter.");
			throw new SmtpException(SmtpException.INVALID_SIZE_PARAM);
		}
		long maxMessageSize = Config.getMaxMessageSize();
		if ((maxMessageSize > 0) && (size > maxMessageSize)) {
			StringBuilder errorBuffer = new StringBuilder(256)
					.append("Rejected message from ")
					.append(session.getClientDomain())
					.append(" from host ")
					.append(session.getRemoteHost())
					.append(" (")
					.append(session.getRemoteIP())
					.append(") exceeding system maximum message size of ")
					.append(maxMessageSize)
					.append(" based on SIZE option.");
			logger.error(errorBuffer.toString());
			throw new SmtpException(SmtpException.MESSAGE_SIZE_LIMIT);
		}
	}
	
}
