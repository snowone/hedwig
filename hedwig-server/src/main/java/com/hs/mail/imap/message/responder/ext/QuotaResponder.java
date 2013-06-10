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
package com.hs.mail.imap.message.responder.ext;

import javax.mail.Quota;

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.DefaultImapResponder;
import com.hs.mail.imap.message.response.ext.QuotaResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 19, 2010
 *
 */
public class QuotaResponder extends DefaultImapResponder {

	public QuotaResponder(Channel channel, ImapRequest request) {
		super(channel, request);
	}

	public void respond(QuotaResponse response) {
		composeQuotaRoot(response);
		composeQuota(response.getQuota());
	}

	private void composeQuotaRoot(QuotaResponse response) {
		if (response.getMailbox() != null) {
			untagged("QUOTAROOT");
			quote(response.getMailbox());
			quote(response.getQuota().quotaRoot);
			end();
		}
	}

	private void composeQuota(Quota quota) {
		untagged("QUOTA");
		quote(quota.quotaRoot);
		openParen("(");
		composeResouces(quota.resources);
		closeParen(")");
		end();
	}

	private void composeResouces(Quota.Resource[] resources) {
		if (resources != null) {
			for (int i = 0; i < resources.length; i++) {
				message(resources[i].name);
				message(resources[i].usage);
				message(resources[i].limit);
			}
		}
	}

}
