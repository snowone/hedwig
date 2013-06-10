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
package com.hs.mail.imap.message.response;

import com.hs.mail.imap.dao.DaoFactory;
import com.hs.mail.imap.dao.MailboxDao;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 19, 2010
 *
 */
public class SelectResponseBuilder {
	
	public SelectResponse build(UidToMsnMapper map, Mailbox mailbox) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		SelectResponse response = new SelectResponse(mailbox);
		response.setMessageCount(dao.getMessageCount(mailbox.getMailboxID()));
		if (response.getMessageCount() > 0) {
			long firstUnseen = dao.getFirstUnseenMessageID(mailbox
					.getMailboxID());
			if (firstUnseen > 0)
				response.setFirstUnseen(map.getMessageNumber(firstUnseen));
			response.setRecentMessageCount(dao.getRecentMessageCount(mailbox
					.getMailboxID()));
		}
		return response;
	}

}
