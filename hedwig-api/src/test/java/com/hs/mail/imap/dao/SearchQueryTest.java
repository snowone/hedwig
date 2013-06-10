package com.hs.mail.imap.dao;

import javax.mail.Flags;

import junit.framework.TestCase;

import com.hs.mail.imap.message.search.CompositeKey;
import com.hs.mail.imap.message.search.FlagKey;

public class SearchQueryTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testToQuery() throws Exception {
		String query = SearchQuery.toQuery(1, new CompositeKey(new FlagKey(
				Flags.Flag.FLAGGED, false)));
		assertNotNull(query);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
