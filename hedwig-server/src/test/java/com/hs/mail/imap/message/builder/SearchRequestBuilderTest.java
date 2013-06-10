package com.hs.mail.imap.message.builder;

import java.io.StringReader;
import java.util.LinkedList;

import junit.framework.TestCase;

import com.hs.mail.imap.message.ImapRequestFactory;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.SearchRequest;
import com.hs.mail.imap.message.search.AndKey;
import com.hs.mail.imap.message.search.FromStringKey;
import com.hs.mail.imap.message.search.HeaderKey;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SequenceKey;
import com.hs.mail.imap.parser.CommandParser;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.ImapMessage;
import com.hs.mail.imap.test.MockImapMessage;

public class SearchRequestBuilderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSequence() throws Exception {
		SequenceRange[] range = new SequenceRange[] {
				new SequenceRange(100, Long.MAX_VALUE), new SequenceRange(110),
				new SequenceRange(200, 201),
				new SequenceRange(400, Long.MAX_VALUE) };
		check(new SearchKey[] { new SequenceKey(range) },
				"T SEARCH *:100,110,200:201,400:*");
	}
	
	public void testUidSequence() throws Exception {
		SequenceRange[] range = new SequenceRange[] {
				new SequenceRange(100, Long.MAX_VALUE), new SequenceRange(110),
				new SequenceRange(200, 201),
				new SequenceRange(400, Long.MAX_VALUE) };
		check(new SearchKey[] { new SequenceKey(range, true) },
				"T SEARCH UID *:100,110,200:201,400:*");
	}
	
	public void testFromHeader() throws Exception {
		check(new SearchKey[] { new HeaderKey("FROM", "Smith") },
				"T SEARCH HEADER FROM Smith");
	}
	
	public void testStringUnquoted() throws Exception {
		check(new SearchKey[] { new FromStringKey("Smith") },
				"T SEARCH FROM Smith");
	}

	public void testStringQuoted() throws Exception {
		check(new SearchKey[] { new FromStringKey("Smith And Jones") },
				"T SEARCH FROM \"Smith And Jones\"");
	}
	
	private void check(SearchKey[] keys, String command) {
		ImapMessage message = new MockImapMessage(parse(command));
		ImapRequest request = ImapRequestFactory.createImapRequest(message);
		assertTrue(request instanceof SearchRequest);
		SearchKey searchKey = null;
		if (keys.length > 1) {
			searchKey = new AndKey();
			for (int i = 0; i < keys.length; i++) {
				((AndKey) searchKey).addKey(keys[i]);
			}
		} else {
			searchKey = keys[0];
		}
		assertEquals(((SearchRequest) request).getSearchKey(), searchKey);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private LinkedList<Token> parse(String command) {
		CommandParser parser = new CommandParser(new StringReader(command + "\r\n"));
		return parser.command();
	}
	
}
