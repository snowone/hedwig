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

import java.util.List;

import javax.mail.Quota;

import com.hs.mail.imap.user.Alias;
import com.hs.mail.imap.user.User;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 17, 2010
 *
 */
public interface UserDao {

	public User getUser(long id);

	public long getUserID(String address);
	
	public User getUserByAddress(String address);
	
	public int getUserCount(String domain);
	
	public List<User> getUserList(String domain, int page, int pageSize);
	
	public long addUser(User user);
	
	public int updateUser(User user);
	
	public int deleteUser(long id);
	
	public Alias getAlias(long id);
	
	public int getAliasCount(String domain);

	public List<Alias> getAliasList(String domain, int page, int pageSize);
	
	public List<Alias> expandAlias(String alias);

	public long addAlias(Alias alias);
	
	public int updateAlias(Alias alias);

	public int deleteAlias(long id);

	public long getQuotaLimit(long ownerID);
	
	public long getQuotaUsage(long ownerID);

	public Quota getQuota(long ownerID, String quotaRoot);

	public void setQuota(long ownerID, Quota quota);

}
