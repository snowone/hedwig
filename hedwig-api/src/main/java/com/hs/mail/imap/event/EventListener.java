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

/**
 * The common interface for all event listeners
 * 
 * @author Won Chul Doh
 * @since Jul 30, 2010
 * 
 */
public interface EventListener {

	/**
	 * Invoked when an event was fired.
	 */
	void event(final Event event);

	public interface Event {

		public final static long ANONYMOUS_SESSION_ID = 0;

		/**
		 * Returns the ID of session who triggered this event.
		 * 
		 * @return ID of the session
		 */
		long getSessionID();

		/**
		 * Returns the ID of the mailbox where this event was triggered.
		 * 
		 * @return ID of the mailbox
		 */
		long getMailboxID();
	
	}

}
