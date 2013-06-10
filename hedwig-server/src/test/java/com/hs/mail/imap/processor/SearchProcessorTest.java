package com.hs.mail.imap.processor;

//import static org.mockito.Mockito.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.SearchRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SequenceKey;
import com.hs.mail.imap.message.search.SortKey;

public class SearchProcessorTest extends AbstractImapProcessorTest {

    private static final String COMMAND = "SEARCH";

	Responder responder;
	SearchProcessor processor = new MockSearchProcessor();

	protected void setUp() throws Exception {
		super.setUp();
		responder = mock(Responder.class);
	}
	
    public void testSequenceSetLowerUnlimited() throws Exception {
    	expectGetSelectedMailbox();
    	SequenceRange[] sequenceSet = { new SequenceRange(1, Long.MAX_VALUE) };
    	SequenceKey key = new SequenceKey(sequenceSet);
    	check(key);
    }

    @SuppressWarnings("unchecked")
	private void expectGetSelectedMailbox() throws Exception {
		when(session.getSelectedMailbox()).thenReturn(selectedMailbox);
		when(selectedMailbox.getMailboxID()).thenReturn(1L);
		when(selectedMailbox.getCachedUids()).thenReturn(Collections.EMPTY_LIST);
    }
    
	private void check(SearchKey key) {
		when(
				mailboxManager.search(any(UidToMsnMapper.class), eq(1L),
						eq(key), (List<SortKey>) isNull())).thenReturn(
				new ArrayList<Long>());

		SearchRequest message = new SearchRequest(TAG, COMMAND, null, key, true);
		processor.doProcess(session, message, responder);

		verify(responder, times(1)).okCompleted(message);
	}
    
    static class MockSearchProcessor extends SearchProcessor {
    	protected MailboxManager getMailboxManager() {
    		return mailboxManager;
    	}
    }
    
}
