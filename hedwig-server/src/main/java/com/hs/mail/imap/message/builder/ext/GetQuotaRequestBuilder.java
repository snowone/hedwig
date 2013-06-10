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

import com.hs.mail.imap.message.builder.ImapRequestBuilder;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.ext.GetQuotaRequest;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 19, 2010
 *
 */
public class GetQuotaRequestBuilder extends ImapRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message) {
		LinkedList<Token> tokens = message.getTokens();
		String quotaRoot = tokens.remove().value;
		return new GetQuotaRequest(tag, command, quotaRoot);
	}

}
