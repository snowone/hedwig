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
package com.hs.mail.imap.processor;

import java.util.ArrayList;
import java.util.List;

import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.MailboxQuery;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class ListProcessor extends AbstractListProcessor {

	@Override
	protected List<Mailbox> listMailbox(long userID, long ownerID,
			String mailboxName, MailboxQuery query) {
		MailboxManager manager = getMailboxManager();
		List<Mailbox> children = manager.getChildren(userID, ownerID,
				mailboxName, false);
		List<Mailbox> results = new ArrayList<Mailbox>();
		for (Mailbox child : children) {
			if (query.match(child.getName())) {
				child.setHasChildren(manager.hasChildren(child));
				results.add(child);
			}
		}
		return results;
	}

	@Override
	protected Mailbox getMailbox(long ownerID, String mailboxName) {
		MailboxManager manager = getMailboxManager();
		Mailbox result = manager.getMailbox(ownerID, mailboxName);
		if (result != null) {
			result.setHasChildren(manager.hasChildren(result));
		}
		return result;
	}

}
