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

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.message.request.DeleteRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.HumanReadableText;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class DeleteProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		DeleteRequest request = (DeleteRequest) message;
		String mailboxName = request.getMailbox();
		if (ImapConstants.INBOX_NAME.equalsIgnoreCase(mailboxName)) {
			responder.taggedNo(request,
					HumanReadableText.FAILED_TO_DELETE_INBOX);
		} else {
			MailboxManager manager = getMailboxManager();
			Mailbox mailbox = manager.getMailbox(session.getUserID(),
					mailboxName);
			if (mailbox == null) {
				responder
						.taggedNo(request, HumanReadableText.MAILBOX_NOT_FOUND);
			} else {
				// Check for inferior hierarchical names
				if (!mailbox.isNoInferiors() && manager.hasChildren(mailbox)) {
					// Check for \Noselect mailbox name attribute
					if (!mailbox.isNoSelect()) {
						// Remove all messages and set \Noselect mailbox
						// name attribute
						manager.deleteMailbox(session.getUserID(), mailbox
								.getMailboxID(), false);
					}
				} else {
					manager.deleteMailbox(session.getUserID(), mailbox
							.getMailboxID(), true);
					fireMailboxDeleted(session, mailbox.getMailboxID());
				}
				SelectedMailbox selected = session.getSelectedMailbox();
				if (selected != null
						&& selected.getMailboxID() == mailbox.getMailboxID()) {
					manager.removeEventListener(selected);
					session.deselect();
				}
				responder.okCompleted(request);
			}
		}
	}
	
	private void fireMailboxDeleted(ImapSession session, long mailboxID) {
		getMailboxManager().getEventDispatcher().mailboxDeleted(
				session.getSessionID(), mailboxID);
	}

}
