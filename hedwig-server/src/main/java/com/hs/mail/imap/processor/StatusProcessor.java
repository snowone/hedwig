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
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.Status;
import com.hs.mail.imap.message.request.StatusRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.responder.StatusResponder;
import com.hs.mail.imap.message.response.HumanReadableText;
import com.hs.mail.imap.message.response.StatusResponse;
import com.hs.mail.imap.message.response.StatusResponseBuilder;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class StatusProcessor extends AbstractImapProcessor {

	private StatusResponseBuilder builder = new StatusResponseBuilder();
	
	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		doProcess(session, (StatusRequest) message, (StatusResponder) responder);
	}

	private void doProcess(ImapSession session, StatusRequest request,
			StatusResponder responder) {
		String mailboxName = request.getMailbox();
		Status attr = request.getStatusAtts();
		MailboxManager manager = getMailboxManager();
		Mailbox mailbox = manager.getMailbox(session.getUserID(), mailboxName);
		if (mailbox == null) {
			responder.taggedNo(request, HumanReadableText.MAILBOX_NOT_FOUND);
		} else {
			StatusResponse response = builder.build(attr, mailbox);
			responder.respond(response);
			responder.okCompleted(request);
		}
	}
	
	@Override
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new StatusResponder(channel, request);
	}

}
