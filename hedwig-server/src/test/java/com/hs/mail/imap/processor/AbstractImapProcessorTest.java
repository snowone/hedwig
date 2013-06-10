package com.hs.mail.imap.processor;

import static org.mockito.Mockito.mock;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.dao.DaoFactory;
import com.hs.mail.imap.dao.MailboxDao;
import com.hs.mail.imap.dao.MessageDao;
import com.hs.mail.imap.dao.SearchDao;
import com.hs.mail.imap.dao.UserDao;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.SelectedMailbox;
import com.hs.mail.imap.user.UserManager;

import junit.framework.TestCase;

public abstract class AbstractImapProcessorTest extends TestCase {

	protected static final String TAG = "TAG";
	
	protected static MailboxManager mailboxManager = mock(MailboxManager.class);
	protected static UserManager userManager = mock(UserManager.class);
	
	protected ImapSession session;
	protected SelectedMailbox selectedMailbox;
	
	protected void setUp() throws Exception {
		super.setUp();
		setUpDaoFactory();
		session = mock(ImapSession.class);
		selectedMailbox = mock(SelectedMailbox.class);
	}
	
	private void setUpDaoFactory() {
		DaoFactory factory = DaoFactory.getInstance();
		factory.setMailboxDao(mock(MailboxDao.class));
		factory.setMessageDao(mock(MessageDao.class));
		factory.setSearchDao(mock(SearchDao.class));
		factory.setUserDao(mock(UserDao.class));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
