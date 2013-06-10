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
package com.hs.mail.imap.processor;

import javax.security.auth.login.LoginException;

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.LoginRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.user.UserManager;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 31, 2010
 *
 */
public class LoginProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		LoginRequest request = (LoginRequest) message;
		String username = request.getUsername();
		String password = request.getPassword();
		UserManager manager = getUserManager();
		try {
			long userid = manager.login(username, password);
			session.authenticated(userid);
			MailboxManager mailboxManager = getMailboxManager();
			final String inboxName = ImapConstants.INBOX_NAME;
			if (!mailboxManager.mailboxExists(userid, inboxName)) {
				if (logger.isDebugEnabled())
					logger.debug("INBOX does not exist for " + username
							+ ". Creating it.");
				mailboxManager.createMailbox(userid, inboxName);
			}
			responder.okCompleted(request);
		} catch (LoginException e) {
			responder.taggedNo(request, "failed. " + e.getMessage());
		}
	}

}
