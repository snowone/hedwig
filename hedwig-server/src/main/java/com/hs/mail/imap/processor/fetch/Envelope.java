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
package com.hs.mail.imap.processor.fetch;

import java.io.Serializable;

/**
 * The envelope structure if the message. This is computed by the server by
 * parsing the [RFC-2822] header into the component parts, defaulting various
 * fields as necessary.
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 * 
 */
public class Envelope implements Serializable {

	private static final long serialVersionUID = -2711420810570566424L;

	private Address[] bcc;

	private Address[] cc;

	private String date;

	private Address[] from;

	private String inReplyTo;

	private String messageId;

	private Address[] replyTo;

	private Address[] sender;

	private String subject;

	private Address[] to;

	public Envelope(String date, String subject, Address[] from,
			Address[] sender, final Address[] replyTo, Address[] to,
			Address[] cc, final Address[] bcc, String inReplyTo,
			String messageId) {
		super();
		this.date = date;
		this.subject = subject;
		this.from = from;
		this.sender = sender;
		this.replyTo = replyTo;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.inReplyTo = inReplyTo;
		this.messageId = messageId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Address[] getBcc() {
		return bcc;
	}

	public Address[] getCc() {
		return cc;
	}

	public Address[] getFrom() {
		return from;
	}

	public Address[] getReplyTo() {
		return replyTo;
	}

	public Address[] getSender() {
		return sender;
	}

	public Address[] getTo() {
		return to;
	}

}
