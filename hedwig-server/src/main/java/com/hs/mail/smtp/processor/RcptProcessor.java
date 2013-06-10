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
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * Handler for RCPT command. Read recipient. Does some recipient verification.
 * 
 * @author Won Chul Doh
 * @since May 29, 2010
 * 
 */
public class RcptProcessor extends AbstractSmtpProcessor {

	@Override
	protected void doProcess(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws SmtpException {
		SmtpMessage message = session.getMessage();
		if (message == null || message.getFrom() == null) {
			throw new SmtpException(SmtpException.COMMAND_OUT_OF_SEQUENCE);
		}
		if (st.countTokens() < 1) {
			throw new SmtpException(SmtpException.INVALID_COMMAND_PARAM);
		}
		String to = nextToken(st);
		if (!startsWith(to, "TO:")) {
			throw new SmtpException(SmtpException.INVALID_COMMAND_PARAM);
		}
		if (to.length() == 3) {
			if (!st.hasMoreTokens()) {
				throw new SmtpException(SmtpException.MISSING_RECIPIENT_ADDRESS);
			}
			to = nextToken(st);
		} else {
			to = to.substring(3);
		}
		if ("postmaster".equalsIgnoreCase(to)) {
			to = Config.getPostmaster();
		}
		int maxRcpt = Config.getMaxRcptCount();
		if (maxRcpt > 0) {
			int rcptCount = message.getRecipientsSize();
			if (rcptCount >= maxRcpt) {
				throw new SmtpException(SmtpException.RECIPIENTS_COUNT_LIMIT);
			}
		}
		Recipient recipient = new Recipient(to);
		String toDomain = recipient.getHost();
		if (!Config.isLocal(toDomain)) {
			if (!Config.getAuthorizedNetworks().matches(
					session.getClientAddress())) {
				throw new SmtpException(SmtpException.RELAY_DENIED);
			}
		}
		message.addRecipient(recipient);
		session.writeResponse("250 2.1.5 Recipient <" + recipient.getMailbox()
					+ "> OK");
	}

}
