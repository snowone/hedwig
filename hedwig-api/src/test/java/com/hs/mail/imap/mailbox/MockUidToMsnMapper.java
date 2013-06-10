package com.hs.mail.imap.mailbox;

import com.hs.mail.imap.dao.DaoFactory;

public class MockUidToMsnMapper extends UidToMsnMapper {

	public MockUidToMsnMapper(long mailboxID) {
		super(null, null, false);
		this.uids = DaoFactory.getMessageDao().getMessageIDList(mailboxID);
	}
	
	public MockUidToMsnMapper(long mailboxID, boolean useUID) {
		super(null, useUID);
		this.uids = DaoFactory.getMessageDao().getMessageIDList(mailboxID);
	}

	public long getUID(int msgnum) {
		return (msgnum <= uids.size()) ? (Long) uids.get(msgnum - 1) : -1;
	}

	public int getMessageNumber(long uid) {
		if (uid == Long.MAX_VALUE) { // *
			return (uids.size() > 0) ? uids.size() : -1;
		} else {
			int i = uids.indexOf(uid);
			return (i >= 0) ? i + 1 : -1;
		}
	}

}
