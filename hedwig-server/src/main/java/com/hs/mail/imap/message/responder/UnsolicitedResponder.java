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

import org.apache.commons.collections.CollectionUtils;

import com.hs.mail.imap.message.response.StoreResponse;
import com.hs.mail.imap.message.response.UnsolicitedResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Aug 1, 2010
 * 
 */
public class UnsolicitedResponder extends StoreResponder {

	public UnsolicitedResponder(Responder responder) {
		super(responder.getChannel(), responder.getRequest());
	}

	public void respond(UnsolicitedResponse response) {
		if (response.isSizeChanged()) {
			// New message response
			untagged(response.getMessageCount() + " EXISTS\r\n");
			untagged(response.getRecentMessageCount() + " RECENT\r\n");
		} else {
			if (CollectionUtils.isNotEmpty(response.getExpungedMsns())) {
				// Expunged messages
				for (Integer msn : response.getExpungedMsns()) {
					untagged(msn + " " + "EXPUNGE\r\n");
				}
			}
			if (CollectionUtils.isNotEmpty(response.getFlagsResponses())) {
				// Message updates
				for (StoreResponse flagResponse : response.getFlagsResponses()) {
					respond(flagResponse);
				}
			}
		}
	}

}
