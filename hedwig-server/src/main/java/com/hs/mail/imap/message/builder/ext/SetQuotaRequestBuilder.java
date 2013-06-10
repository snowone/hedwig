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
package com.hs.mail.imap.message.builder.ext;

import java.util.LinkedList;

import javax.mail.Quota;

import com.hs.mail.imap.message.builder.ImapRequestBuilder;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.ext.SetQuotaRequest;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 19, 2010
 *
 */
public class SetQuotaRequestBuilder extends ImapRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message) {
		LinkedList<Token> tokens = message.getTokens();
		Quota quota = new Quota(tokens.remove().value);
		Token token = tokens.peek(); // consume '('
		if (token.type == Token.Type.LPAREN) {
			tokens.remove(); // remove left parenthesis
			do {
				if ((token = tokens.remove()).type == Token.Type.RPAREN)
					break;
				String name = token.value;
				long limit = Long.parseLong(tokens.remove().value);
				quota.setResourceLimit(name, limit);
			} while (true);
		}
		return new SetQuotaRequest(tag, command, quota);
	}

}
