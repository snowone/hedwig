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
package com.hs.mail.imap.message.request;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class Status {

	private boolean messages = false;
	private boolean recent = false;
	private boolean uidNext = false;
	private boolean uidValidity = false;
	private boolean unseen = false;

	public boolean isMessages() {
		return messages;
	}

	public void setMessages(boolean messages) {
		this.messages = messages;
	}

	public boolean isRecent() {
		return recent;
	}

	public void setRecent(boolean recent) {
		this.recent = recent;
	}

	public boolean isUidNext() {
		return uidNext;
	}

	public void setUidNext(boolean uidNext) {
		this.uidNext = uidNext;
	}

	public boolean isUidValidity() {
		return uidValidity;
	}

	public void setUidValidity(boolean uidValidity) {
		this.uidValidity = uidValidity;
	}

	public boolean isUnseen() {
		return unseen;
	}

	public void setUnseen(boolean unseen) {
		this.unseen = unseen;
	}

}
