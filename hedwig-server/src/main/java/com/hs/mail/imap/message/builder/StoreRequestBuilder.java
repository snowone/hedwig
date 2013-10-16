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

import java.util.LinkedList;

import javax.mail.Flags;

import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.StoreRequest;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.DecoderUtils;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class StoreRequestBuilder extends AbstractUidRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message, boolean useUID) {
		LinkedList<Token> tokens = message.getTokens();
		SequenceRange[] sequenceSet = parseSequenceSet(tokens);
		Token token = tokens.remove();
		char c = token.value.charAt(0);
		Boolean sign = null;
		if (c == '+') {
			sign = Boolean.TRUE;
		} else if (c == '-') {
			sign = Boolean.FALSE;
		}
		boolean silent = token.value.indexOf('.') > 0;
		Flags flags = parseFlagList(tokens);
		if (flags == null) {
			flags = new Flags();
			while (!tokens.isEmpty()) {
				token = tokens.remove();
				DecoderUtils.decodeFlagList(token.value, flags);
			}
		}
		return new StoreRequest(tag, command, sequenceSet, sign, silent, flags,
				useUID);
	}

}
