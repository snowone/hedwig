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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.message.request.CloseRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.Responder;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class CloseProcessor extends AbstractExpungeProcessor {
	
	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		CloseRequest request = (CloseRequest) message;
		SelectedMailbox selected = session.getSelectedMailbox();
		MailboxManager manager = getMailboxManager();
		if (!selected.isReadOnly()) {
			// Permanently removes all messages that have the \Deleted flag set
			// if the mailbox is selected read-write.
			List<Long> expungedUids = manager.expunge(selected.getMailboxID());
			if (CollectionUtils.isNotEmpty(expungedUids)) {
				// Propagate event before delete message.
				fireExpunged(session, expungedUids);
				for (Long uid : expungedUids) {
					manager.deleteMessage(uid);
				}
				selected.resetEvents();
			}
			if (selected.isRecent()) {
				// Clear \Recent flag for the messages in this mailbox if the
				// mailbox has recent messages.
				manager.resetRecent(selected.getMailboxID());
			}
		}
		manager.removeEventListener(selected);
		session.deselect();
		responder.okCompleted(request);
	}

}
