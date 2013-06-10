package com.hs.mail.imap.mailbox;

import java.util.Arrays;

import junit.framework.TestCase;

public class UidToMsnMapperTest extends TestCase {

	UidToMsnMapper map;
	
	protected void setUp() throws Exception {
		super.setUp();
		map = new UidToMsnMapper(new SelectedMailbox(1L, 1L, true), 
					Arrays.asList(new Long[] { 100L, 101L, 102L, 105L }), 
					true);
	}

	public void testGetUID() throws Exception {
		assertEquals(101L, map.getUID(2));
		assertEquals(-1L, map.getUID(5));
	}
	
	public void testGetMessageNumber() throws Exception {
		assertEquals(3L, map.getMessageNumber(102L));
		assertEquals(-1L, map.getMessageNumber(103L));
	}
	
	public void testGetMinMaxMessageNumber() throws Exception {
		assertEquals(1L, map.getMaxMessageNumber(100L));
		assertEquals(3L, map.getMaxMessageNumber(103L));
		assertEquals(4L, map.getMinMessageNumber(103L));
		assertEquals(4L, map.getMaxMessageNumber(Long.MAX_VALUE));
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
