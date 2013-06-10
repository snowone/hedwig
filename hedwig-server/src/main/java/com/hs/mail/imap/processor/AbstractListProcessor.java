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

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxQuery;
import com.hs.mail.imap.message.request.AbstractListRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.ListResponder;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.ListResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 16, 2010
 *
 */
public abstract class AbstractListProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		doProcess(session, (AbstractListRequest) message,
				(ListResponder) responder);
	}

	private void doProcess(ImapSession session, AbstractListRequest request,
			ListResponder responder) {
		String referenceName = request.getMailbox();
		String mailboxName = request.getPattern();
		if (StringUtils.isEmpty(mailboxName)) {
			// An empty mailbox name argument is a special request
			// to return the hierarchy delimiter and the root name of the name
			// given in the reference.
			String referenceRoot = "";
			if (referenceName.startsWith(ImapConstants.NAMESPACE_PREFIX)) {
				// A qualified reference name - get the first element.
				int i = referenceName.indexOf(Mailbox.folderSeparator);
				if (i != -1) {
					referenceRoot = referenceName.substring(0, i + 1);
				}
			}
			responder.untagged(request.getCommand() + " (\\Noselect) \""
					+ Mailbox.folderSeparator + "\" \"" + referenceRoot
					+ "\"\r\n");
		} else {
			if (mailboxName.startsWith(ImapConstants.NAMESPACE_PREFIX)) {
				// If the mailboxName if fully qualified, ignore the reference
				// name.
				referenceName = "";
			} else {
				// Remove separator from the end of reference name.
				referenceName = StringUtils.chomp(referenceName,
						Mailbox.folderSeparator);
			}
			doList(session, responder, referenceName, mailboxName);
		}
		responder.okCompleted(request);
	}

	@Override
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new ListResponder(channel, request);
	}

	protected abstract List<Mailbox> listMailbox(long userID, long ownerID,
			String mailboxName, MailboxQuery query);
	
	protected abstract Mailbox getMailbox(long ownerID, String mailboxName);

	protected void doList(ImapSession session, ListResponder responder,
			String referenceName, String mailboxName) {
		MailboxQuery query = new MailboxQuery(referenceName, mailboxName);
		if (query.containsWildcard()) {
			List<Mailbox> mailboxes = listMailbox(session.getUserID(),
					session.getUserID(), referenceName, query);
			for (Mailbox mailbox : mailboxes) {
				responder.respond(new ListResponse(mailbox));
			}
		} else {
			// Expression is an absolute mailbox name.
			Mailbox mailbox = getMailbox(session.getUserID(),
					query.getExpression());
			if (mailbox != null) {
				responder.respond(new ListResponse(mailbox));
			}
		}
	}
	
}
