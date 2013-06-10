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
import com.hs.mail.imap.message.response.SelectResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 19, 2010
 *
 */
public class SelectResponder extends DefaultImapResponder {

	public SelectResponder(Channel channel, ImapRequest request) {
		super(channel, request);
	}

	public void respond(SelectResponse response) {
		untagged("FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft)\r\n");
		untagged(response.getMessageCount() + " EXISTS\r\n");
		untagged(response.getRecentMessageCount() + " RECENT\r\n");
		if (response.getFirstUnseen() > 0) {
			untaggedOK("[UNSEEN " + response.getFirstUnseen()
					+ "] First unseen");
		}
		untaggedOK("[UIDNEXT " + response.getNextUid() + "] Next UID");
		untaggedOK("[UIDVALIDITY " + response.getUidValidity() + "] UID Valid");
		if (response.isReadOnly()) {
			untaggedOK("[PERMANENTFLAGS ()] Read-only mailbox");
		} else {
			untaggedOK("[PERMANENTFLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft \\*)] Limited");
		}
	}

}
