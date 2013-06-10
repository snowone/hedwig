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
public class LsubProcessor extends AbstractListProcessor {

	@Override
	protected List<Mailbox> listMailbox(long userID, long ownerID,
			String mailboxName, MailboxQuery query) {
		MailboxManager manager = getMailboxManager();
		List<Mailbox> children = manager.getChildren(userID, ownerID,
				mailboxName, true);
		List<Mailbox> results = new ArrayList<Mailbox>();
		for (Mailbox child : children) {
			addSubscription(manager, children, query, child, false, results);
		}
		return results;
	}

	@Override
	protected Mailbox getMailbox(long ownerID, String mailboxName) {
		MailboxManager manager = getMailboxManager();
		Mailbox result = manager.getMailbox(ownerID, mailboxName);
		if (result != null
				&& manager.isSubscribed(ownerID, result.getName())) {
			result.setHasChildren(manager.hasChildren(result));
		}
		return result;
	}
	
	private void addSubscription(MailboxManager manager,
			List<Mailbox> subscriptions, MailboxQuery query, Mailbox mailbox,
			boolean noSelect, List<Mailbox> results) {
		if (query.match(mailbox.getName())) {
			if (!results.contains(mailbox)) {
				mailbox.setNoSelect(noSelect);
				mailbox.setHasChildren(manager.hasChildren(mailbox));
				results.add(mailbox);
			}
		} else {
			String parentName = Mailbox.getParent(mailbox.getName());
			if (!"".equals(parentName) && !results.contains(parentName)) {
				addSubscription(manager, subscriptions, query, new Mailbox(
						parentName), true, results);
			}
		}
	}

}
