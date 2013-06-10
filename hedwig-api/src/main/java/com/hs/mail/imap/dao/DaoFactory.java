/*
 * Copyright 2010 the original author or authors.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hs.mail.imap.dao;

/**
 * Static factory to conceal the automatic choice of the data access object
 * implementation class.
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 * 
 */
public class DaoFactory {

	private static DaoFactory instance;

	private MailboxDao mailboxDao;
	private MessageDao messageDao;
	private SearchDao searchDao;
	private UserDao userDao;

	public static DaoFactory getInstance() {
		if (null == instance) {
			instance = new DaoFactory();
		}
		return instance;
	}

	public void setMailboxDao(MailboxDao mailboxDao) {
		this.mailboxDao = mailboxDao;
	}

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}
	
	public void setSearchDao(SearchDao searchDao) {
		this.searchDao = searchDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public static MailboxDao getMailboxDao() {
		return getInstance().mailboxDao;
	}

	public static MessageDao getMessageDao() {
		return getInstance().messageDao;
	}

	public static SearchDao getSearchDao() {
		return getInstance().searchDao;
	}

	public static UserDao getUserDao() {
		return getInstance().userDao;
	}

}
