package com.hs.mail.imap.processor;

//import static org.mockito.Mockito.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.request.ListRequest;
import com.hs.mail.imap.message.responder.ListResponder;
import com.hs.mail.imap.message.response.ListResponse;

public class ListProcessorTest extends AbstractImapProcessorTest {

    private static final String COMMAND = "LIST";
	
	Mailbox result;
	ListResponder responder;
	ListProcessor processor = new MockLostProcessor();
	
	protected void setUp() throws Exception {
		super.setUp();
		result = mock(Mailbox.class);
		responder = mock(ListResponder.class);
	}

    public void testHasChildren() throws Exception {
        expectSession();
    	setUpResult("INBOX");
		when(mailboxManager.getChildren(1L, 1L, "", false)).thenReturn(Arrays.asList(new Mailbox[] { result }));
		when(mailboxManager.hasChildren(eq(result))).thenReturn(true);
		
		ListRequest message = new ListRequest(TAG, COMMAND, "", "*");
		processor.doProcess(session, message, responder);
		
		verify(responder, times(1)).respond(any(ListResponse.class));
		verify(responder, times(1)).okCompleted(message);
    }
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void setUpResult(String name) {
		when(result.getName()).thenReturn(name);
	}
	
    private void expectSession() throws Exception {
    	when(session.getUserID()).thenReturn(1L);
    }
    
    static class MockLostProcessor extends ListProcessor {
    	protected MailboxManager getMailboxManager() {
    		return mailboxManager;
    	}
    }
	
}
