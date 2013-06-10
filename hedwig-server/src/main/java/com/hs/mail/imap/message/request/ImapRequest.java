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

import com.hs.mail.imap.ImapSession;

/**
 * An IMAP request.
 * 
 * @author Won Chul Doh
 * @since Jan 14, 2010
 * 
 */
public abstract class ImapRequest {
	
	private String tag;
	private String command;

	/**
	 * Constructor for AbstractCommand.
	 */
	protected ImapRequest(String tag, String command) {
		super();
		this.tag = tag;
		this.command = command;
	}

	public String getTag() {
		return tag;
	}

	public String getCommand() {
		return command.toUpperCase();
	}
	
	public abstract boolean validForState(ImapSession.State state);

}
