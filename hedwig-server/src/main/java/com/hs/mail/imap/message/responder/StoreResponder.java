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

import javax.mail.Flags;

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.response.StoreResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 29, 2010
 *
 */
public class StoreResponder extends DefaultImapResponder {

	public StoreResponder(Channel channel, ImapRequest request) {
		super(channel, request);
	}
	
	public void respond(StoreResponse response) {
		untagged(response.getMessageNumber() + " FETCH");
		openParen("(");
		composeFlags(response);
		closeParen(")");
		end();
	}

	void composeFlags(StoreResponse response) {
		Flags flags = response.getFlags();
		if (flags != null) {
			message("FLAGS");
			openParen("(");
			flags(flags);
			closeParen(")");
		}
	}
	
}
