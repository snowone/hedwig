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
import com.hs.mail.imap.message.request.Status;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 17, 2010
 *
 */
public class StatusResponse extends AbstractImapResponse {

	private Status status;
	private Mailbox mailbox;
	private int messageCount;
	private int recentMessageCount;
	private int unseenMessageCount;

	public StatusResponse(Status status, Mailbox mailbox) {
		this.status = status;
		this.mailbox = mailbox;
	}

	public Status getStatusAtts() {
		return status;
	}

	public String getMailboxName() {
		return mailbox.getName();
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int count) {
		this.messageCount = count;
	}

	public int getRecentMessageCount() {
		return recentMessageCount;
	}

	public void setRecentMessageCount(int count) {
		this.recentMessageCount = count;
	}

	public long getNextUid() {
		return mailbox.getNextUID();
	}

	public long getUidValidity() {
		return mailbox.getUidValidity();
	}

	public int getUnseenMessageCount() {
		return unseenMessageCount;
	}

	public void setUnseenMessageCount(int count) {
		this.unseenMessageCount = count;
	}

}
