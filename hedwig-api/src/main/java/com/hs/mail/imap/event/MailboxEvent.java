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
package com.hs.mail.imap.event;

import com.hs.mail.imap.event.EventListener.Event;

/**
 * Common base class for mailbox events.
 * 
 * @author Won Chul Doh
 * @since Aug 4, 2010
 * 
 */
public abstract class MailboxEvent implements Event {

	private long sessionID;
	private long mailboxID;

	protected MailboxEvent(long sessionID, long mailboxID) {
		this.sessionID = sessionID;
		this.mailboxID = mailboxID;
	}
	
	public long getSessionID() {
		return sessionID;
	}
	
	public long getMailboxID() {
		return mailboxID;
	}

}
