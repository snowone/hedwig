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

import java.util.Set;

import org.apache.log4j.Logger;

import com.hs.mail.imap.dao.DaoFactory;
import com.hs.mail.imap.dao.MailboxDao;
import com.hs.mail.imap.dao.MessageDao;
import com.hs.mail.imap.event.EventTracker;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.mailbox.UidToMsnMapper;

/**
 * 
 * @author Won Chul Doh
 * @since Aug 1, 2010
 *
 */
public class UnsolicitedResponseBuilder {
	
	static Logger logger = Logger.getLogger(UnsolicitedResponseBuilder.class);

	public UnsolicitedResponse build(SelectedMailbox selected,
			EventTracker tracker) {
		if (!tracker.isSizeChanged() && !tracker.hasExpungedUids()
				&& !tracker.hasUpdatedFlags()) {
			// No change to the mailbox
			return null;
		}
		UnsolicitedResponse response = new UnsolicitedResponse(tracker
				.isSizeChanged());
		if (tracker.isSizeChanged()) {
			setExistsResponses(selected, response);
		} else if (tracker.hasExpungedUids() || tracker.hasUpdatedFlags()) {
			UidToMsnMapper map = new UidToMsnMapper(selected, false);
			if (tracker.hasExpungedUids()) {
				setExpungedResponses(response, tracker, map);
			}
			if (tracker.hasUpdatedFlags()) {
				setFlagsResponses(response, tracker, map);
			}
		}
		return response;
	}
	
	private void setExistsResponses(SelectedMailbox selected,
			UnsolicitedResponse response) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		response.setMessageCount(dao.getMessageCount(selected.getMailboxID()));
		if (response.getMessageCount() > 0) {
			response.setRecentMessageCount(dao.getRecentMessageCount(selected
					.getMailboxID()));
		}
	}
	
	private void setExpungedResponses(UnsolicitedResponse response,
			EventTracker tracker, UidToMsnMapper map) {
		Set<Long> expungedUids = tracker.getExpungedUids();
		for (Long messageID : expungedUids) {
			int msgnum = map.getMessageNumber(messageID);
			if (msgnum != -1) {
				response.addExpungedMsn(msgnum);
			} else {
				// This case is impossible.
				logger.error("Failed to convert UID " + messageID
						+ " to message number.");
			}
		}
	}
	
	private void setFlagsResponses(UnsolicitedResponse response,
			EventTracker tracker, UidToMsnMapper map) {
		MessageDao dao = DaoFactory.getMessageDao();
		Set<Long> flagUpdatedUids = tracker.getFlagUpdatedUids();
		for (Long messageID : flagUpdatedUids) {
			int msgnum = map.getMessageNumber(messageID);
			if (msgnum != -1) {
				response.addFlagsResponse(msgnum, dao.getFlags(messageID));
			} else {
				// This case is impossible.
				logger.error("Failed to convert UID " + messageID
						+ " to message number.");
			}
		}
	}

}
