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
package com.hs.mail.imap.processor.ext;

import javax.mail.Quota;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.ext.SetQuotaRequest;
import com.hs.mail.imap.message.responder.ext.QuotaResponder;
import com.hs.mail.imap.message.response.HumanReadableText;
import com.hs.mail.imap.message.response.ext.QuotaResponse;
import com.hs.mail.imap.user.UserManager;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 19, 2010
 *
 */
public class SetQuotaProcessor extends AbstractQuotaProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			QuotaResponder responder) throws Exception {
		SetQuotaRequest request = (SetQuotaRequest) message;
		Quota quota = request.getQuota();
		if (!"".equals(quota.quotaRoot)) {
			responder.taggedNo(request, HumanReadableText.NO_SUCH_QUOTA_ROOT);
		} else {
			UserManager manager = getUserManager();
			manager.setQuota(session.getUserID(), quota);
			responder.respond(new QuotaResponse(quota));
			responder.okCompleted(request);
		}
	}

}
