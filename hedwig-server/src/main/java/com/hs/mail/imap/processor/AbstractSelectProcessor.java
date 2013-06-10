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

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.request.AbstractMailboxRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.responder.SelectResponder;
import com.hs.mail.imap.message.response.HumanReadableText;
import com.hs.mail.imap.message.response.SelectResponse;
import com.hs.mail.imap.message.response.SelectResponseBuilder;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 24, 2010
 *
 */
public abstract class AbstractSelectProcessor extends AbstractImapProcessor {
	
	private SelectResponseBuilder builder = new SelectResponseBuilder();

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		doProcess(session, (AbstractMailboxRequest) message,
				(SelectResponder) responder);
	}

	private void doProcess(ImapSession session, AbstractMailboxRequest request,
			SelectResponder responder) {
		String mailboxName = request.getMailbox();
		MailboxManager manager = getMailboxManager();
		Mailbox mailbox = manager.getMailbox(session.getUserID(), mailboxName);
		if (mailbox == null) {
			responder.taggedNo(request, HumanReadableText.MAILBOX_NOT_FOUND);
		} else if (mailbox.isNoSelect()) {
			responder.taggedNo(request,
					HumanReadableText.MAILBOX_NOT_SELECTABLE);
		} else {
			SelectedMailbox selected = session.getSelectedMailbox();
			if (selected != null && !selected.isReadOnly()
					&& selected.isRecent()) {
				// If the session is read-write, subsequent sessions will not see
				// \Recent set for the messages in this mailbox.
				manager.resetRecent(selected.getMailboxID());
			}
			long sessionID = session.getSessionID();
			long mailboxID = mailbox.getMailboxID();
			selected = new SelectedMailbox(sessionID, mailboxID, isReadOnly());
			UidToMsnMapper map = new UidToMsnMapper(selected, false);
			mailbox.setReadOnly(isReadOnly());
			SelectResponse response = builder.build(map, mailbox);
			responder.respond(response);
			
			selected.setRecent(response.getRecentMessageCount() > 0);
			session.selected(selected);
			manager.addEventListener(selected);
			
			responder.okCompleted(request, "[" + getResponseCode() + "]");
		}
	}
	
	private String getResponseCode() {
		return isReadOnly() ? "READ-ONLY" : "READ-WRITE";
	}
	
	@Override
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new SelectResponder(channel, request);
	}
	
	protected abstract boolean isReadOnly();

}
