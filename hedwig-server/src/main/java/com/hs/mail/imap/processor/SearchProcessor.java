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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.SearchRequest;
import com.hs.mail.imap.message.responder.Responder;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class SearchProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) {
		SearchRequest request = (SearchRequest) message;
		SelectedMailbox selected = session.getSelectedMailbox();
		MailboxManager manager = getMailboxManager();
		List<Long> uids = selected.getCachedUids();
		if (uids == null) {
			uids = manager.getMessageIDList(selected.getMailboxID());
		}
		UidToMsnMapper map = new UidToMsnMapper(selected, uids, request.isUseUID());
		List<Long> results = search(manager, map, selected.getMailboxID(),
				request);
		if (CollectionUtils.isNotEmpty(results)) {
			List<Long> searched = new ArrayList<Long>();
			for (long messageID : results) {
				if (request.isUseUID()) {
					searched.add(messageID);
				} else {
					long msgnum = map.getMessageNumber(messageID);
					if (msgnum != -1) {
						searched.add(msgnum);
					}
				}
			}
			responder.untagged(request.getCommand() + " "
					+ StringUtils.join(searched, ' ') + "\r\n");
		}
		responder.okCompleted(request);
	}
	
	protected List<Long> search(MailboxManager manager, UidToMsnMapper map,
			long mailboxID, SearchRequest request) {
		return manager.search(map, mailboxID, request.getSearchKey(), null);
	}

}
