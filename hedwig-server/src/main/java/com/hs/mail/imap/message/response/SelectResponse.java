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
package com.hs.mail.imap.message.response;

import com.hs.mail.imap.mailbox.Mailbox;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 19, 2010
 *
 */
public class SelectResponse extends AbstractImapResponse {

	private Mailbox mailbox;
	private long firstUnseen = 0;
	private int messageCount = 0;
	private int recentMessageCount = 0;
	
	public SelectResponse(Mailbox mailbox) {
		super();
		this.mailbox = mailbox;
	}

	/**
	 * Gets the UID of the first unseen message.
	 * 
	 * @return UID of the first unseen message, zero if not exist unseen message
	 */
	public long getFirstUnseen() {
		return firstUnseen;
	}

	public void setFirstUnseen(long firstUnseen) {
		this.firstUnseen = firstUnseen;
	}

	/**
	 * Gets the number of messages that this mailbox contains.
	 * 
	 * @return number of messages contained
	 */
	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int count) {
		this.messageCount = count;
	}

	/**
	 * Gets the number of recent messages.
	 * 
	 * @return number of recent messages
	 */
	public int getRecentMessageCount() {
		return recentMessageCount;
	}

	public void setRecentMessageCount(int count) {
		this.recentMessageCount = count;
	}

	/**
	 * Gets the next unique identifier predicted.
	 * 
	 * @return the UID that will be assigned to the next appended message
	 */
	public long getNextUid() {
		return mailbox.getNextUID();
	}

	/**
	 * Gets the unique identifier validity value.
	 * 
	 * @return UIDVALIDITY
	 */
	public long getUidValidity() {
		return mailbox.getUidValidity();
	}
	
	public boolean isReadOnly() {
		return mailbox.isReadOnly();
	}

}
