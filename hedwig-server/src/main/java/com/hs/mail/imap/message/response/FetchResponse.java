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

import java.util.Date;

import javax.mail.Flags;

import com.hs.mail.imap.processor.fetch.Content;
import com.hs.mail.imap.processor.fetch.Envelope;
import com.hs.mail.imap.processor.fetch.MimeDescriptor;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class FetchResponse extends AbstractImapResponse {
	
	private long msgnum;
	private Long uid;
	/**
	 * The [RFC-2822] size of the message.
	 */
	private Long size;
	/**
	 * The flags that are set for this message.
	 */
	private Flags flags;
	/**
	 * The internal date of this message.
	 */
	private Date internalDate;
	private Envelope envelope;
	private MimeDescriptor body;
	private MimeDescriptor bodyStructure;
	private Content content;
	
	public FetchResponse(long msgnum) {
		this.msgnum = msgnum;
	}
	
	public long getMessageNumber() {
		return msgnum;
	}

	public Long getUid() {
		return uid;
	}
	
	public void setUid(Long uid) {
		this.uid = uid;
	}
	
	public Flags getFlags() {
		return flags;
	}
	
	public void setFlags(Flags flags) {
		this.flags = flags;
	}

	public Long getSize() {
		return size;
	}
	
	public void setSize(Long size) {
		this.size = size;
	}

	public Date getInternalDate() {
		return internalDate;
	}

	public void setInternalDate(Date internalDate) {
		this.internalDate = internalDate;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	public void setBody(MimeDescriptor body) {
		this.body = body;
	}

	public MimeDescriptor getBody() {
		return body;
	}

	public void setBodyStructure(MimeDescriptor bodyStructure) {
		this.bodyStructure = bodyStructure;
	}

	public MimeDescriptor getBodyStructure() {
		return bodyStructure;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

}
