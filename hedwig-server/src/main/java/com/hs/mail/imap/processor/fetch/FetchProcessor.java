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
package com.hs.mail.imap.processor.fetch;

import javax.mail.FetchProfile;

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.FetchData;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.FetchRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.FetchResponder;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.FetchResponse;
import com.hs.mail.imap.message.response.FetchResponseBuilder;
import com.hs.mail.imap.processor.AbstractImapProcessor;

/**
 * Handler for FETCH command.
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class FetchProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		doProcess(session, (FetchRequest) message, (FetchResponder) responder);
	}
	
	private void doProcess(ImapSession session, FetchRequest request,
			FetchResponder responder) {
		SelectedMailbox selected = session.getSelectedMailbox();
		UidToMsnMapper map = new UidToMsnMapper(selected, request.isUseUID());
		MailboxManager manager = getMailboxManager();
		FetchResponseBuilder builder = new FetchResponseBuilder(manager);
		SequenceRange[] sequenceSet = request.getSequenceSet();
		FetchProfile fp = request.getFetchProfile();
		for (int i = 0; i < sequenceSet.length; i++) {
			long min = map.getMinMessageNumber(sequenceSet[i].getMin());
			long max = map.getMaxMessageNumber(sequenceSet[i].getMax());
			for (long j = min; j <= max && j >= 0; j++) {
				long uid = map.getUID((int) j);
				if (uid != -1) {
					FetchData fd = manager.getMessageFetchData(uid);
					if (fd != null) {
						FetchResponse response = builder.build(j, fp, fd);
						if (response != null) {
							responder.respond(response);
						}
					}
				} else {
					break; // Out of index
				}
			}
		}
		responder.okCompleted(request);
	}
	
	@Override
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new FetchResponder(channel, request);
	}

}
