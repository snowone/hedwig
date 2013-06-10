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

import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.response.StatusResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 17, 2010
 *
 */
public class StatusResponder extends DefaultImapResponder {

	public StatusResponder(Channel channel, ImapRequest request) {
		super(channel, request);
	}
	
	public void respond(StatusResponse response) {
		untagged(request.getCommand());
		quote(response.getMailboxName());
		openParen("(");
		composeMessages(response);
		composeRecent(response);
		composeUidNext(response);
		composeUidValidity(response);
		composeUnseen(response);
		closeParen(")");
		end();
	}
	
	void composeMessages(StatusResponse response) {
		if (response.getStatusAtts().isMessages()) {
			message("MESSAGES");
			message(response.getMessageCount());
		}
	}
	
	void composeRecent(StatusResponse response) {
		if (response.getStatusAtts().isRecent()) {
			message("RECENT");
			message(response.getRecentMessageCount());
		}
	}
	
	void composeUidNext(StatusResponse response) {
		if (response.getStatusAtts().isUidNext()) {
			message("UIDNEXT");
			message(response.getNextUid());
		}
	}
	
	void composeUidValidity(StatusResponse response) {
		if (response.getStatusAtts().isUidValidity()) {
			message("UIDVALIDITY");
			message(response.getUidValidity());
		}
	}
	
	void composeUnseen(StatusResponse response) {
		if (response.getStatusAtts().isUnseen()) {
			message("UNSEEN");
			message(response.getUnseenMessageCount());
		}
	}

}
