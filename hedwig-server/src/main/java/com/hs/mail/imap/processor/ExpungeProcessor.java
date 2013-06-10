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
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.request.ExpungeRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.HumanReadableText;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class ExpungeProcessor extends AbstractExpungeProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		ExpungeRequest request = (ExpungeRequest) message;
		SelectedMailbox selected = session.getSelectedMailbox();
		if (selected.isReadOnly()) {
			responder.taggedNo(request, "[READ-ONLY]",
					HumanReadableText.MAILBOX_IS_READ_ONLY);
			return;
		}
		MailboxManager manager = getMailboxManager();
		// Permanently removes all messages that have the \Deleted flag set.
		List<Long> expungedUids = manager.expunge(selected.getMailboxID());
		if (CollectionUtils.isNotEmpty(expungedUids)) {
			UidToMsnMapper map = new UidToMsnMapper(selected, false);
			// Propagate event before delete message.
			fireExpunged(session, expungedUids);
			for (Long uid : expungedUids) {
				int msgnum = map.getMessageNumber(uid);
				if (msgnum != -1) {
					manager.deleteMessage(uid);
					responder.untagged(msgnum + " " + request.getCommand()
							+ "\r\n");
				} else {
					// This case is impossible.
					logger.error("Failed to convert UID " + uid
							+ " to message number.");
				}
			}
			selected.resetEvents();
		}
		responder.okCompleted(request);
	}

}
