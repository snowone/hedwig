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

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.CopyRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.HumanReadableText;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class CopyProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		CopyRequest request = (CopyRequest) message;
		String mailboxName = request.getMailbox();
		SelectedMailbox selected = session.getSelectedMailbox();
		MailboxManager manager = getMailboxManager();
		Mailbox mailbox = manager.getMailbox(session.getUserID(), mailboxName);
		if (mailbox == null) {
			// SHOULD NOT automatically create the mailbox.
			responder.taggedNo(request, "[TRYCREATE]", HumanReadableText.MAILBOX_NOT_FOUND);
		} else {
			UidToMsnMapper map = new  UidToMsnMapper(selected, request.isUseUID());
			SequenceRange[] sequenceSet = request.getSequenceSet();
			for (int i = 0; i < sequenceSet.length; i++) {
				long min = map.getMinMessageNumber(sequenceSet[i].getMin());
				long max = map.getMaxMessageNumber(sequenceSet[i].getMax());
				for (long j = min; j <= max && j >= 0; j++) {
					long uid = map.getUID((int) j);
					if (uid != -1) {
						manager.copyMessage(uid, mailbox.getMailboxID());
					} else {
						break; // Out of index
					}
				}
			}
			responder.okCompleted(request);
		}
	}

}
