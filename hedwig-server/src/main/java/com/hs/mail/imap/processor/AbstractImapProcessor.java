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

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.hs.mail.container.config.ComponentManager;
import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.event.EventTracker;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.DefaultImapResponder;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.responder.UnsolicitedResponder;
import com.hs.mail.imap.message.response.HumanReadableText;
import com.hs.mail.imap.message.response.UnsolicitedResponse;
import com.hs.mail.imap.message.response.UnsolicitedResponseBuilder;
import com.hs.mail.imap.user.UserManager;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 31, 2010
 *
 */
public abstract class AbstractImapProcessor implements ImapProcessor {
	
	static Logger logger = Logger.getLogger(AbstractImapProcessor.class);

	private UnsolicitedResponseBuilder builder = new UnsolicitedResponseBuilder();
	
	protected AbstractImapProcessor() {
		super();
	}
	
	public void process(ImapSession session, ImapRequest request,
			Channel channel) {
		if (!request.validForState(session.getState())) {
			channel.write(request.getTag() + " NO " + request.getCommand()
					+ " " + HumanReadableText.INVALID_COMMAND + "\r\n");
		} else {
			Responder responder = createResponder(channel, request);
			if (isSelectedMailboxDeleted(session)) {
				writeSignOff(session, responder);
			} else {
				try {
					doProcess(session, request, responder);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					responder.taggedNo(request, ex.getMessage());
				}
			}
		}
	}
	
	abstract protected void doProcess(ImapSession session, ImapRequest request,
			Responder responder) throws Exception;

	protected MailboxManager getMailboxManager() {
		return (MailboxManager) ComponentManager
				.getBeanOfType(MailboxManager.class);
	}
	
	protected UserManager getUserManager() {
		return (UserManager) ComponentManager.getBeanOfType(UserManager.class);
	}
	
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new DefaultImapResponder(channel, request);
	}
	
	protected void unsolicitedResponse(ImapSession session, Responder responder) {
		SelectedMailbox selected = session.getSelectedMailbox();
		if (selected != null) {
			EventTracker tracker = selected.getEventTracker();
			if (tracker != null) {
				UnsolicitedResponse response = builder.build(selected, tracker);
				if (response != null) {
					new UnsolicitedResponder(responder).respond(response);
				}
				selected.resetEvents();
			}
		}
	}
	
	private boolean isSelectedMailboxDeleted(ImapSession session) {
		SelectedMailbox selected = session.getSelectedMailbox();
		return (selected != null) ? selected.isDeletedMailbox() : false;
	}
	
	private void writeSignOff(ImapSession session, Responder responder) {
		getMailboxManager().removeEventListener(session.getSelectedMailbox());
		// Is this necessary?
		session.logout();
		// RFC2180 3.3 The server MAY allow the DELETE/RENAME of a
		// multi-accessed mailbox, but disconnect all other clients who
		// have the mailbox accessed by sending a untagged BYE response.
		ChannelFuture future = responder
				.bye(HumanReadableText.MAILBOX_DELETED_SIGN_OFF);
		future.addListener(ChannelFutureListener.CLOSE);
	}
	
}
