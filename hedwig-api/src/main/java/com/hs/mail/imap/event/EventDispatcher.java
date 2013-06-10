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

import java.util.ArrayList;
import java.util.List;

import com.hs.mail.imap.event.EventListener.Event;

/**
 * This class is responsible for dispatching all events coming from Message
 * Store to all event listeners subscribed in this dispatcher.
 * 
 * @author Won Chul Doh
 * @since Jul 30, 2010
 * 
 */
public class EventDispatcher {

	private final List<EventListener> listeners = new ArrayList<EventListener>();  
	
	/**
	 * Add a listener for events.
	 * 
	 * @param listener
	 *            the listener for events
	 */
	public void addEventListener(EventListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a listener for events.
	 * 
	 * @param listener
	 *            the listener
	 * @see #addEventListener
	 */
	public void removeEventListener(EventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void added(long mailboxID) {
		notifyEventListeners(new MessageAddedEvent(mailboxID));
	}

	public void expunged(long sessionID, long mailboxID, List<Long> uids) {
		notifyEventListeners(new MessageExpungedEvent(sessionID, mailboxID, uids));
	}
	
	public void flagsUpdated(long sessionID, long mailboxID, List<Long> uids) {
		notifyEventListeners(new FlagUpdatedEvent(sessionID, mailboxID, uids));
	}
	
	public void mailboxDeleted(long sessionID, long mailboxID) {
		notifyEventListeners(new MailboxDeletedEvent(sessionID, mailboxID));
	}
	
	/**
	 * Notify all event listeners.
	 * 
	 * @param event
	 *            event to broadcast
	 */
	public void notifyEventListeners(Event event) {
		synchronized (listeners) {
			for (EventListener listener : listeners) {
				listener.event(event);
			}
		}
	}
	
}
