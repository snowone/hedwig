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

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;

/**
 * 
 * @author Won Chul Doh
 * @since Aug 1, 2010
 *
 */
public class UnsolicitedResponse implements ImapResponse {

	private boolean sizeChanged = false;
	private int messageCount = 0;
	private int recentMessageCount = 0;
    private List<Integer> expungedMsns = null;
    private List<StoreResponse> flagsResponses = null;

	public UnsolicitedResponse(boolean sizeChanged) {
		super();
		this.sizeChanged = sizeChanged;
		this.expungedMsns = new ArrayList<Integer>();
		this.flagsResponses = new ArrayList<StoreResponse>();
	}
    
	public boolean isSizeChanged() {
		return sizeChanged;
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

	public List<Integer> getExpungedMsns() {
		return expungedMsns;
	}
	
	public List<StoreResponse> getFlagsResponses() {
		return flagsResponses;
	}

	public void addExpungedMsn(int msgnum) {
		expungedMsns.add(msgnum);
	}

	public void addFlagsResponse(int msgnum, Flags flags) {
		flagsResponses.add(new StoreResponse((long) msgnum, flags));
	}
	
}
