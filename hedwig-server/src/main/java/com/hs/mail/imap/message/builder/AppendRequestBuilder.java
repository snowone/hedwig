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
package com.hs.mail.imap.message.builder;

import java.util.Date;
import java.util.LinkedList;

import javax.mail.Flags;

import com.hs.mail.imap.message.request.AppendRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.parser.ParseException;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.DecoderUtils;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class AppendRequestBuilder extends ImapRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message) {
		LinkedList<Token> tokens = message.getTokens();
		Token token = tokens.remove();
		String mailbox = token.value;
		Flags flags = parseFlagList(tokens);
		Date datetime = null;
		token = tokens.remove();
		if (token.type == Token.Type.DATE_TIME) {
			try {
				datetime = DecoderUtils.parseDateTime(token.value);
			} catch (Exception e) {
				throw new ParseException(tag, e);
			}
		} else {
			datetime = new Date();
		}
		return new AppendRequest(tag, command, mailbox, flags, datetime,
				message.getLiteral());
	}
	
}
