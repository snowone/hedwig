package com.hs.mail.imap.processor;

//import static org.mockito.Mockito.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.request.SelectRequest;
import com.hs.mail.imap.message.responder.SelectResponder;
import com.hs.mail.imap.message.response.HumanReadableText;

public class SelectProcessorTest extends AbstractImapProcessorTest {

	private static final String COMMAND = "SEARCH";
	private static final String MAILBOX_NAME = "MAILBOX_NAME";

	Mailbox mailbox;
	SelectResponder responder;
	SelectProcessor processor = new MockSelectProcessor();

	protected void setUp() throws Exception {
		super.setUp();
		mailbox = mock(Mailbox.class);
		responder = mock(SelectResponder.class);
		when(session.getUserID()).thenReturn(1L);
	}

	public void testNotExist() throws Exception {
		check(false, null, false, HumanReadableText.MAILBOX_NOT_FOUND);
	}

	public void testNoSelect() throws Exception {
		check(true, mailbox, false, HumanReadableText.MAILBOX_NOT_SELECTABLE);
	}

	public void testSelect() throws Exception {
		check(false, mailbox, true, "[READ-WRITE]");
	}

	private void check(boolean noSelect, Mailbox mailbox, boolean success,
			String text) throws Exception {
		// Setup
		if (mailbox != null)
			when(mailbox.isNoSelect()).thenReturn(noSelect);
		when(mailboxManager.getMailbox(eq(1L), eq(MAILBOX_NAME))).thenReturn(
				mailbox);
		// Exercise
		SelectRequest message = new SelectRequest(TAG, COMMAND, MAILBOX_NAME);
		processor.doProcess(session, message, responder);
		// Verify
		if (success)
			verify(responder, times(1)).okCompleted(message, text);
		else
			verify(responder, times(1)).taggedNo(message, text);
	}

	static class MockSelectProcessor extends SelectProcessor {
		protected MailboxManager getMailboxManager() {
			return mailboxManager;
		}
	}

}
