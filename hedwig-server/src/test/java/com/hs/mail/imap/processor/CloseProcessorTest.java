package com.hs.mail.imap.processor;

//import static org.mockito.Mockito.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.request.CloseRequest;
import com.hs.mail.imap.message.responder.Responder;

public class CloseProcessorTest extends AbstractImapProcessorTest {

    private static final String COMMAND = "CLOSE";
	
	Responder responder;
	CloseProcessor processor = new MockCloseProcessor();
	
	protected void setUp() throws Exception {
		super.setUp();
		responder = mock(Responder.class);
	}
	
	public void testReadOnlyClose() throws Exception {
		expectGetSelectedMailbox();
		when(selectedMailbox.isReadOnly()).thenReturn(true);
		
		CloseRequest message = new CloseRequest(TAG, COMMAND);
		processor.doProcess(session, message, responder);
		
		verify(mailboxManager, never()).expunge(anyLong());
		verify(session, times(1)).deselect();
		verify(responder, times(1)).okCompleted(message);
	}
	
	public void testReadWriteClose() throws Exception {
		expectGetSelectedMailbox();
		when(selectedMailbox.isReadOnly()).thenReturn(false);
		when(mailboxManager.getEventDispatcher()).thenAnswer(RETURNS_MOCKS);
		when(mailboxManager.expunge(eq(1L))).thenReturn(
				Arrays.asList(new Long[] { 147L }));
		
		CloseRequest message = new CloseRequest(TAG, COMMAND);
		processor.doProcess(session, message, responder);

		verify(mailboxManager, times(1)).expunge(eq(1L));
		verify(mailboxManager, times(1)).deleteMessage(eq(147L));
		verify(session, times(1)).deselect();
		verify(responder, times(1)).okCompleted(message);
	}

    private void expectGetSelectedMailbox() throws Exception {
		when(session.getSelectedMailbox()).thenReturn(selectedMailbox);
		when(selectedMailbox.getMailboxID()).thenReturn(1L);
    }
    
    static class MockCloseProcessor extends CloseProcessor {
    	protected MailboxManager getMailboxManager() {
    		return mailboxManager;
    	}
    }
    
}
