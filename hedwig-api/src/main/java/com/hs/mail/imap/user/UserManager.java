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
package com.hs.mail.imap.user;

import java.io.File;
import java.util.List;

import javax.mail.Quota;
import javax.security.auth.login.LoginException;

import com.hs.mail.smtp.message.MailAddress;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 24, 2010
 *
 */
public interface UserManager {

	/**
	 * Authenticate the given user against the given password. When
	 * authenticated, the ID of the user will be supplied.
	 * 
	 * @param username
	 *            user name
	 * @param password
	 *            password supplied
	 * @return id of the user when authenticated
	 * @throws LoginException
	 *             when the user does not exist or not authenticated
	 */
	public long login(String username, String password) throws LoginException;
	
	public User getUser(long id);

	public long getUserID(String address);
	
	public User getUserByAddress(String address);

	public int getUserCount(String domain);
	
	public List<User> getUserList(String domain, int page, int pageSize);
	
	public long addUser(final User user);
	
	public int updateUser(final User user);
	
	public void deleteUser(final long id);
	
	public void emptyUser(final long id);
	
	public Alias getAlias(long id);
	
	public int getAliasCount(String domain);
	
	public List<Alias> getAliasList(String domain, int page, int pageSize);
	
	public List<Alias> expandAlias(String alias);
	
	public long addAlias(final Alias alias);
	
	public int updateAlias(final Alias alias);
	
	public void deleteAlias(final long id);
	
	public long getQuotaUsage(long ownerID);

	public Quota getQuota(long ownerID, String quotaRoot);

	public void setQuota(final long ownerID, final Quota quota);
	
	public File getUserHome(MailAddress user);

}
