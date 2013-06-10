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

import java.util.List;

/**
 * This class models Message <code>expunged</code> event. MessgeExpungedEvents
 * are delivered to EventListeners registered with different Session ID and same
 * Mailbox ID.
 * <p>
 * When messages are expunged through operations on objects in other virtual
 * machine, such events may not be notified.
 * </p>
 * 
 * @author Won Chul Doh
 * @since Jul 30, 2010
 * 
 */
public class MessageExpungedEvent extends MessageEvent {

	private List<Long> expungedUids;
	
	public MessageExpungedEvent(long sessionID, long mailboxID,
			List<Long> expungedUids) {
		super(sessionID, mailboxID);
		this.expungedUids = expungedUids;
	}
	
	public List<Long> getExpungedUids() {
		return expungedUids;
	}
	
}
