package com.hs.mail.imap.processor.ext;

import java.util.List;

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.request.SearchRequest;
import com.hs.mail.imap.message.request.ext.SortRequest;
import com.hs.mail.imap.processor.SearchProcessor;

public class SortProcessor extends SearchProcessor {

	@Override
	protected List<Long> search(MailboxManager manager, UidToMsnMapper map,
			long mailboxID, SearchRequest request) {
		SortRequest req = (SortRequest) request;
		return manager.search(map, mailboxID, request.getSearchKey(),
				req.getSortKeys());
	}
	
}
