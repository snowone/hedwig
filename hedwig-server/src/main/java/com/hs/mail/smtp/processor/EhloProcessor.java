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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.hs.mail.container.config.Config;
import com.hs.mail.container.server.socket.TcpTransport;
import com.hs.mail.smtp.SmtpException;
import com.hs.mail.smtp.SmtpSession;

/**
 * Handler for EHLO command. Responds with a greeting and informs the client the
 * supported extensions.
 * 
 * @author Won Chul Doh
 * @since May 29, 2010
 * 
 */
public class EhloProcessor extends AbstractSmtpProcessor {

	@Override
	protected void doProcess(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws SmtpException {
		if (st.countTokens() < 1) {
			throw new SmtpException(SmtpException.MISSING_DOMAIN_ADDRESS);
		} else {
			String clientDomain = nextToken(st);
			if (clientDomain.length() > 256) {
				throw new SmtpException(SmtpException.DOMAIN_NAME_LENGTH_LIMIT);
			} else {
				session.setClientDomain(clientDomain);
				session.setProtocol("ESMTP");
				List<String> extensions = new ArrayList<String>();
				extensions.add(new StringBuilder().append(Config.getHelloName())
						.append(" Hello ")
						.append(session.getRemoteHost())
						.append(" [")
						.append(session.getRemoteIP())
						.append("]").toString());
				long maxMessageSize = Config.getMaxMessageSize();
				if (maxMessageSize > 0) {
					extensions.add("SIZE " + maxMessageSize);
				}
				if (Config.isSaslAuthEnabled()) {
					extensions.add("AUTH LOGIN PLAIN");
					extensions.add("AUTH=LOGIN PLAIN");
				}
				extensions.add("ENHANCEDSTATUSCODES");
				for (String extension : extensions) {
					session.writeResponse("250-" + extension);
				}
				session.writeResponse("250 HELP");
			}
		}
	}

}
