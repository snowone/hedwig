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

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.StoreRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.responder.StoreResponder;
import com.hs.mail.imap.message.response.StoreResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class StoreProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		doProcess(session, (StoreRequest) message, (StoreResponder) responder);
	}
	
	protected void doProcess(ImapSession session, StoreRequest request,
			StoreResponder responder) {
		MailboxManager manager = getMailboxManager();
		SelectedMailbox selected = session.getSelectedMailbox();
		UidToMsnMapper map = new UidToMsnMapper(selected, request.isUseUID());
		SequenceRange[] sequenceSet = request.getSequenceSet();
		List<Long> flagUpdatedUids = new ArrayList<Long>();
		for (int i = 0; i < sequenceSet.length; i++) {
			long min = map.getMinMessageNumber(sequenceSet[i].getMin());
			long max = map.getMaxMessageNumber(sequenceSet[i].getMax());
			for (long j = min; j <= max && j >= 0; j++) {
				long uid = map.getUID((int) j);
				if (uid != -1) {
					manager.setFlags(uid, request.getFlags(), request
							.isReplace(), request.isPlus());
					flagUpdatedUids.add(new Long(uid));
					if (!request.isSilent()) {
						responder.respond(new StoreResponse(j, manager
								.getFlags(uid)));
					}
				} else {
					break; // Out of index
				}
			}
		}
		flagsUpdated(session, flagUpdatedUids);
		responder.okCompleted(request);
	}
	
	private void flagsUpdated(ImapSession session, List<Long> flagUpdatedUids) {
		getMailboxManager().getEventDispatcher().flagsUpdated(
				session.getSessionID(),
				session.getSelectedMailbox().getMailboxID(), flagUpdatedUids);
	}

	@Override
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new StoreResponder(channel, request);
	}
	
}
