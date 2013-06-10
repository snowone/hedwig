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

import java.util.Date;

import javax.mail.Flags;

import org.jboss.netty.buffer.ChannelBuffer;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.ImapSession.State;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 14, 2010
 *
 */
public class AppendRequest extends ImapRequest {

	private final String mailbox;
	private final Flags flags;
	private final Date datetime;
	private final ChannelBuffer message;

	public AppendRequest(String tag, String command, String mailbox,
			Flags flags, Date datetime, ChannelBuffer message) {
		super(tag, command);
		this.mailbox = mailbox;
		this.flags = flags;
		this.datetime = datetime;
		this.message = message;
	}

	public String getMailbox() {
		return mailbox;
	}

	public Flags getFlags() {
		return flags;
	}

	public Date getDatetime() {
		return datetime;
	}

	public ChannelBuffer getMessage() {
		return message;
	}

	@Override
	public boolean validForState(State state) {
		return (state == ImapSession.State.AUTHENTICATED || state == ImapSession.State.SELECTED);
	}

}
