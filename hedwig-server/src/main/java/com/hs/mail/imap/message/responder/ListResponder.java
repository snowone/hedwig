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
package com.hs.mail.imap.message.responder;

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.response.ListResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class ListResponder extends DefaultImapResponder {

	public ListResponder(Channel channel, ImapRequest request) {
		super(channel, request);
	}

	public void respond(ListResponse response) {
		Mailbox mailbox = response.getMailbox();
		untagged(request.getCommand());
		composeNameAttributes(mailbox);
		quote(Mailbox.folderSeparator);
		quote(mailbox.getName());
		end();
	}

	void composeNameAttributes(Mailbox mailbox) {
		openParen("(");
		if (mailbox.isNoInferiors()) {
			message("\\Noinferiors");
		}
		if (mailbox.isNoInferiors()) {
			message("\\Noselect");
		}
		if (mailbox.hasChildren()) {
			message("\\HasChildren");
		} else {
			message("\\HasNoChildren");
		}
		closeParen(")");
	}

}
