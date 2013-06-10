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
package com.hs.mail.imap.message;

import java.util.Date;

import javax.mail.FetchProfile;
import javax.mail.Flags;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 29, 2010
 *
 */
public class FetchData {

	private long messageID;
	private long physMessageID;
	private long size;
	private Flags flags;
	private Date internalDate;

	public FetchData() {
		super();
	}

	public long getMessageID() {
		return messageID;
	}

	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}

	public long getPhysMessageID() {
		return physMessageID;
	}

	public void setPhysMessageID(long physMessageID) {
		this.physMessageID = physMessageID;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Flags getFlags() {
		return flags;
	}

	public void setFlags(Flags flags) {
		this.flags = flags;
	}

	public Date getInternalDate() {
		return internalDate;
	}

	public void setInternalDate(Date internalDate) {
		this.internalDate = internalDate;
	}

	public static class FetchProfileItem extends FetchProfile.Item {

		protected FetchProfileItem(String name) {
			super(name);
		}

		public static final FetchProfileItem HEADERS 
			= new FetchProfileItem("HEADERS");

		public static final FetchProfileItem SIZE 
			= new FetchProfileItem("SIZE");

		public static final FetchProfileItem INTERNALDATE 
			= new FetchProfileItem("INTERNALDATE");

		public static final FetchProfileItem UID 
			= new FetchProfileItem("UID");
		
		public static final FetchProfileItem BODY 
		= new FetchProfileItem("BODY");

		public static final FetchProfileItem BODYSTRUCTURE
			= new FetchProfileItem("BODYSTRUCTURE");

	}

}
