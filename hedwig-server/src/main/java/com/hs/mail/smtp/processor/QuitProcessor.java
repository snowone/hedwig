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

/**
 * Handler for QUIT command. This handler informs the client that the connection
 * is closing.
 * 
 * @author Won Chul Doh
 * @since May 29, 2010
 * 
 */
public class QuitProcessor extends AbstractSmtpProcessor {

	@Override
	protected void doProcess(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws SmtpException {
		if (st.countTokens() > 0) {
			throw new SmtpException(SmtpException.INVALID_COMMAND_PARAM);
		} else {
			session.writeResponse("221 2.0.0 " + Config.getHelloName()
					+ " Service closing transmission channel");
			trans.endSession();
		}
	}

}
