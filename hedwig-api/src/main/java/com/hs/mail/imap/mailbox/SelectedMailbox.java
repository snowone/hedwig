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
package com.hs.mail.imap.mailbox;

import java.util.List;

import org.apache.commons.collections.bidimap.DualHashBidiMap;

import com.hs.mail.imap.event.EventListener;
import com.hs.mail.imap.event.EventTracker;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 31, 2010
 *
 */
public class SelectedMailbox implements EventListener {

	/**
	 * ID of the session who selected this mailbox
	 */
	private long sessionID;
	/**
	 * ID of the selected mailbox
	 */
	private long mailboxID;
	/**
	 * Is this mailbox read only ?
	 */
	private boolean readOnly;
	/**
	 * Whether this mailbox has recent messages?
	 */
	private boolean recent = false;
	/**
	 * Mapping table for UID to MSN and vice versa
	 */
	private DualHashBidiMap converter;
	/**
	 * Keep tracks the notified events.
	 */
	private EventTracker tracker;

	/**
	 * Constructor used to create a selected mailbox.
	 * 
	 * @param sessionID
	 *            ID of the session who selected this mailbox
	 * @param mailboxID
	 *            ID of the selected mailbox
	 * @param readOnly
	 *            Is this mailbox is read-only? If selected via EXAMINE command
	 *            this mailbox is read-only. Otherwise if selected via SELECT
	 *            command this mailbox is not read-only.
	 */
	public SelectedMailbox(long sessionID, long mailboxID, boolean readOnly) {
		this.sessionID = sessionID;
		this.mailboxID = mailboxID;
		this.readOnly = readOnly;
		this.converter = new DualHashBidiMap();
	}
	
	public EventTracker getEventTracker() {
		return tracker;
	}
	
	public List<Long> getCachedUids() {
		return (tracker != null) ? tracker.getCachedUids() : null;
	}
	
	public long getMailboxID() {
		return mailboxID;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Check whether this mailbox contains recent messages.
	 */
	public boolean isRecent() {
		return recent;
	}
	
	/**
	 * Is the mailbox deleted by other session?
	 * 
	 * @return true when the mailbox has been deleted by another session, false
	 *         otherwise
	 */
	public boolean isDeletedMailbox() {
		return (tracker != null) ? tracker.isDeletedMailbox() : false;
	}

	public void setRecent(boolean recent) {
		this.recent = recent;
	}

	/**
	 * Get the message sequence number corresponding to the given UID from the
	 * cache. If not exist, <code>-1</code> is returned.
	 */
	public int getMessageNumber(long uid) {
		Integer v = (Integer) converter.getKey(uid);
		return (v != null) ? v.intValue() : -1;
	}

	/**
	 * Get the UID corresponding to the given message sequence number from the
	 * cache. If not exist, <code>-1</code> is returned.
	 */
	public long getUID(int msn) {
		Long v = (Long) converter.get(msn);
		return (v != null) ? v.longValue() : -1;
	}
	
	public void add(int msn, long uid) {
		converter.put(new Integer(msn), new Long(uid));
	}
	
	public void event(Event event) {
		if (sessionID != event.getSessionID()
				&& mailboxID == event.getMailboxID()) {
			if (tracker == null) {
				// Instantiate event tracker if necessary
				tracker = new EventTracker();
			}
			tracker.event(event);
		}
	}
	
	public void resetEvents() {
		converter.clear();
		if (tracker != null) {
			tracker.reset();
			tracker = null;
		}
	}
	
}
