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

import org.apache.commons.lang.StringUtils;

/**
 * This class implements object representing user who has account in the Mail
 * server.
 * 
 * @author Won Chul Doh
 * @since Mar 17, 2010
 * 
 */
public class User {
	
	private long id;

	/**
	 * The user's email address.
	 */
	private String userID;

	/**
	 * User's login password.
	 */
	private String passwd;
	
	/**
	 * The mail address to which this user's email is forwarded.
	 */
	private String forwardTo;
	
	private long quota;
	
	public User() {
		super();
	}

	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getPassword() {
		return passwd;
	}

	public void setPassword(String passwd) {
		this.passwd = passwd;
	}

	public String getForwardTo() {
		return forwardTo;
	}

	public void setForwardTo(String forwardTo) {
		this.forwardTo = forwardTo;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}
	
	public String getUserName() {
		return StringUtils.substringBefore(userID, "@");
	}

}
