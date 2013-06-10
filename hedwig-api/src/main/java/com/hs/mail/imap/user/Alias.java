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
 * This class represents user's alias which will be expanded to local user's
 * email address.
 * 
 * @author Won Chul Doh
 * @since Jun 24, 2010
 * 
 */
public class Alias {

	private long id;

	/**
	 * The alias email address.
	 */
	private String alias;

	/**
	 * The ID of local user to which mail should be delivered.
	 */
	private long deliverTo;

	/**
	 * Local address to which mail should be delivered.
	 */
	private String userID;

	public Alias() {
		super();
	}

	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public long getDeliverTo() {
		return deliverTo;
	}

	public void setDeliverTo(long deliverTo) {
		this.deliverTo = deliverTo;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getAliasName() {
		return StringUtils.substringBefore(alias, "@");
	}
	
}
