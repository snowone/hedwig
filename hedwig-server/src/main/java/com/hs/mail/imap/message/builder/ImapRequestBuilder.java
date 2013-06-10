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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Flags;

import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.DecoderUtils;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public abstract class ImapRequestBuilder {

	public abstract ImapRequest createRequest(String tag, String command,
			ImapMessage message);

	protected SequenceRange[] parseSequenceSet(LinkedList<Token> tokens) {
		Token token = tokens.remove();
		List<SequenceRange> rangeList = new ArrayList<SequenceRange>();
		do {
			rangeList.add(DecoderUtils.parseSeqRange(token.value));
			token = tokens.peek();
			if (token != null && ",".equals(token.value)) {
				tokens.remove(); // remove a comma
				token = tokens.remove();
			} else {
				break;
			}
		} while (true);
		return (SequenceRange[]) rangeList.toArray(new SequenceRange[rangeList
				.size()]);
	}

	protected Flags parseFlagList(LinkedList<Token> tokens) {
		Flags flags = null;
		Token token = tokens.peek();
		if (token.type == Token.Type.LPAREN) {
			tokens.remove(); // remove left parenthesis
			flags = new Flags();
			do {
				if ((token = tokens.remove()).type == Token.Type.RPAREN)
					break;
				DecoderUtils.decodeFlagList(token.value, flags);
			} while (true);
		}
		return flags;
	}

}
