package com.hs.mail.imap.mailbox;

import junit.framework.TestCase;

public class MailboxQueryTest extends TestCase {

	public void testWildCard() throws Exception {
		MailboxQuery mq = new MailboxQuery("", "this.%.%.%.h*");
		assertTrue(mq.match("this.is.a.mailbox.hierarchy"));
		assertTrue(!mq.match("this.hierarchy"));
	}

	public void testNotEmptyReferenceName() throws Exception {
		MailboxQuery mq = new MailboxQuery("#news", "comp.mail.*");
		assertTrue(mq.match("#news.comp.mail.imap"));
		assertTrue(mq.match("#news.comp.mail.imap.protocol"));
		assertTrue(!mq.match("#news.comp.wiki.user"));
	}
	
}
