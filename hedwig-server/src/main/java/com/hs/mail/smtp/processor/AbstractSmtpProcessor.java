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

import org.apache.log4j.Logger;

import com.hs.mail.container.config.ComponentManager;
import com.hs.mail.container.server.socket.TcpTransport;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.smtp.SmtpException;
import com.hs.mail.smtp.SmtpSession;

/**
 * 
 * @author Won Chul Doh
 * @since May 30, 2010
 *
 */
public abstract class AbstractSmtpProcessor implements SmtpProcessor {

	protected static Logger logger = Logger.getLogger(AbstractSmtpProcessor.class);
	
	public void process(SmtpSession session, TcpTransport trans,
			StringTokenizer st) {
		try {
			doProcess(session, trans, st);
		} catch (SmtpException ex) {
			session.writeResponse(ex.getMessage());
		}
	}
	
	abstract protected void doProcess(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws SmtpException;

	protected UserManager getUserManager() {
		return (UserManager) ComponentManager.getBean("userManager");
	}
	
	protected String nextToken(StringTokenizer st) {
		return st.nextToken().trim();
	}
	
	protected boolean startsWith(String str, String prefix) {
		return (str != null) ? str.toUpperCase().startsWith(prefix) : false;
	}
	
}
