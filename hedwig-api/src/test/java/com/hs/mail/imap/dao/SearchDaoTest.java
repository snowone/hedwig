package com.hs.mail.imap.dao;

import com.hs.mail.test.AbstractTransactionalTestCase;

public class SearchDaoTest extends AbstractTransactionalTestCase {

	private static final String TEST_DATA_FILE = "/dbunit/dbunit-test-data.xml";
	
	public void onSetUp() throws Exception {
		//setTestDataFiles(new String[] { TEST_DATA_FILE } );
		super.onSetUp();
	}

	public void testSearchQuery() throws Exception {
		dumpTable("headername");
	}
	
	public void onTearDown() throws Exception {
		super.onTearDown();
	}
	
}
