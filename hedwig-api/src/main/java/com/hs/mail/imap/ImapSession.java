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
package com.hs.mail.imap;

import java.util.Random;

import com.hs.mail.imap.mailbox.SelectedMailbox;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class ImapSession {
	
	/**
	 * Enumerates RFC3501 session states.
	 */
	public enum State {
		/**
		 * RFC3501 <code>3.1 Not Authenticated State</code>
		 */
		NON_AUTHENTICATED("NON_AUTHENTICATED"),

		/**
		 * RFC3501 <code>3.2 Authenticated State</code>
		 */
		AUTHENTICATED("AUTHENTICATED"),

		/**
		 * RFC3501 <code>3.3 Selected State</code>
		 */
		SELECTED("SELECTED"),

		/**
		 * RFC3501 <code>3.4 Logout State</code>
		 */
		LOGOUT("LOGOUT");

		/** To aid debugging */
		private final String name;

		private State(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

    private final static Random random = new Random();
	
	private long sessionID;
	private State state = State.NON_AUTHENTICATED;
	private long userID = -1;
	private SelectedMailbox selectedMailbox = null; 
	
	public ImapSession() {
		this.sessionID = random.nextLong();
	}
	
	public long getSessionID() {
		return sessionID;
	}

	public State getState() {
		return state;
	}
	
	public long getUserID() {
		return userID;
	}

	public SelectedMailbox getSelectedMailbox() {
		return selectedMailbox;
	}

	public void authenticated() {
		this.state = State.AUTHENTICATED;
	}

	public void authenticated(long userid) {
		this.state = State.AUTHENTICATED;
		this.userID = userid;
	}
	
	public void selected(SelectedMailbox mailbox) {
		this.state = State.SELECTED;
		close();
		this.selectedMailbox = mailbox;
	}
	
	public void deselect() {
		this.state = State.AUTHENTICATED;
		close();
	}
	
	public void logout() {
		this.state = State.LOGOUT;
		close();
	}
	
	private void close() {
		if (this.selectedMailbox != null) {
			this.selectedMailbox = null;
		}
	}

}
