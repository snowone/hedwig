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
package com.hs.mail.web.fetchmail;

import java.util.Date;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 2, 2010
 *
 */
public class FetchAccount implements Comparable<FetchAccount> {

	private long id;
	
	private String name;
	
	private long userID;
	
	private String protocol;

	private String userName;

	private String serverName;

	private String password;

	private int port = 110;
	
	private boolean useSSL = false;

	private boolean autoEmpty = false;

	private boolean autoFetch = false;

	private long fetchInterval;

	private String lastXUID;

	private Date lastReceivedDate;

	private int failureCount = 0;

	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public boolean isAutoEmpty() {
		return autoEmpty;
	}

	public void setAutoEmpty(boolean autoEmpty) {
		this.autoEmpty = autoEmpty;
	}

	public boolean isAutoFetch() {
		return autoFetch;
	}

	public void setAutoFetch(boolean autoFetch) {
		this.autoFetch = autoFetch;
	}

	public long getFetchInterval() {
		return fetchInterval;
	}

	public void setFetchInterval(long fetchInterval) {
		this.fetchInterval = fetchInterval;
	}

	public String getLastXUID() {
		return lastXUID;
	}

	public void setLastXUID(String lastXUID) {
		this.lastXUID = lastXUID;
	}

	public Date getLastReceivedDate() {
		return lastReceivedDate;
	}

	public void setLastReceivedDate(Date lastReceivedDate) {
		this.lastReceivedDate = lastReceivedDate;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public boolean equals(Object obj) {
		if (obj instanceof FetchAccount) {
			FetchAccount account = (FetchAccount) obj;
			return userName.equals(account.getUserName())
					&& serverName.equals(account.getServerName());
		} else {
			return false;
		}
	}

	public int compareTo(FetchAccount o) {
		return (int) (id - o.id);
	}
	
}
