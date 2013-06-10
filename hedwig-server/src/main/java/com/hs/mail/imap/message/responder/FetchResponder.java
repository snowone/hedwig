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
package com.hs.mail.imap.message.responder;

import java.util.Date;

import javax.mail.Flags;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.response.FetchResponse;
import com.hs.mail.imap.processor.fetch.Address;
import com.hs.mail.imap.processor.fetch.Content;
import com.hs.mail.imap.processor.fetch.Envelope;
import com.hs.mail.imap.processor.fetch.MimeDescriptor;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class FetchResponder extends DefaultImapResponder {
	
	public FetchResponder(Channel channel, ImapRequest request) {
		super(channel, request);
	}

	public void respond(FetchResponse response) {
		untagged(response.getMessageNumber() + " " + request.getCommand());
		openParen("(");
		composeFlags(response);
		composeInternalDate(response);
		composeSize(response);
		composeEnvelope(response);
		composeBody(response);
		composeBodyStructure(response);
		composeUid(response);
		composeBodyContent(response);
		closeParen(")");
		end();
	}

	void composeFlags(FetchResponse response) {
		Flags flags = response.getFlags();
		if (flags != null) {
			message("FLAGS");
			openParen("(");
			flags(flags);
			closeParen(")");
		}
	}
	
	void composeInternalDate(FetchResponse response) {
		Date internalDate = response.getInternalDate();
		if (internalDate != null) {
			message("INTERNALDATE");
			quote(encodeDateTime(internalDate));
		}
	}
	
	void composeSize(FetchResponse response) {
		Long size = response.getSize();
		if (size != null) {
			message("RFC822.SIZE");
			message(size.toString());
		}
	}
	
	void composeUid(FetchResponse response) {
		Long uid = response.getUid();
		if (uid != null) {
			message("UID");
			message(uid.toString());
		}
	}
	
	void composeEnvelope(Envelope envelope, boolean prefixWithName) {
		if (envelope != null) {
			if (prefixWithName) {
				message("ENVELOPE");
			}
			openParen("(");
			nillableQuote(envelope.getDate());
			nillableQuote(envelope.getSubject());
			address(envelope.getFrom());
			address(envelope.getSender());
			address(envelope.getReplyTo());
			address(envelope.getTo());
			address(envelope.getCc());
			address(envelope.getBcc());
			nillableQuote(envelope.getInReplyTo());
			nillableQuote(envelope.getMessageId());
			closeParen(")");
		}
	}
	
	void composeEnvelope(FetchResponse response) {
		Envelope envelope = response.getEnvelope();
		composeEnvelope(envelope, true);
	}
	
	void composeBody(FetchResponse response) {
		MimeDescriptor body = response.getBody();
		if (body != null) {
			message("BODY");
			composeStructure(body, false, false);
		}
	}
	
	void composeBodyStructure(FetchResponse response) {
		MimeDescriptor bodyStructure = response.getBodyStructure();
		if (bodyStructure != null) {
			message("BODYSTRUCTURE");
			composeStructure(bodyStructure, true, false);
		}
	}
	
	void composeStructure(MimeDescriptor descriptor, boolean includeExtensions,
			boolean isInnerPart) {
		String mediaType;
		String subType;
		if ((mediaType = descriptor.getType()) == null) {
			mediaType = "text";
			subType = "plain";
		} else {
			subType = descriptor.getSubType();
		}
		composeStructure(descriptor, mediaType, subType, includeExtensions,
				isInnerPart);
	}
	
	void composeStructure(MimeDescriptor descriptor, String mediaType,
			String subType, boolean includeExtensions, boolean isInnerPart) {
		openParen("(");
		if ("multipart".equalsIgnoreCase(mediaType)) {
			composeMultipart(descriptor, mediaType, subType, includeExtensions);
		} else if ("message".equalsIgnoreCase(mediaType)
				&& "rfc822".equalsIgnoreCase(subType)) {
			composeRfc822(descriptor, mediaType, subType, includeExtensions);
		} else {
			composeBasic(descriptor, mediaType, subType, includeExtensions);
		}
		closeParen(")");
	}

	void composeMultipart(MimeDescriptor descriptor, String mediaType,
			String subType, boolean includeExtensions) {
		for (MimeDescriptor part : descriptor.getParts()) {
			composeStructure(part, includeExtensions, true);
		}
		quoteUpper(subType);
		if (includeExtensions) {
			nillableQuotes(descriptor.getParameters());
			composeDisposition(descriptor);
			nillableQuotes(descriptor.getLanguages());
			nillableQuote(descriptor.getLocation());
		}
	}
	
	void composeRfc822(MimeDescriptor descriptor, String mediaType,
			String subType, boolean includeExtensions) {
		composeBodyFields(descriptor, mediaType, subType);
		composeEnvelope(descriptor.getEnvelope(), false);
		composeStructure(descriptor.getEmbeddedMessageDescriptor(),
				includeExtensions, true);
		message(descriptor.getLines());
		if (includeExtensions) {
			encodeOnePartBodyExtensions(descriptor);
		}
	}
	
	void composeBasic(MimeDescriptor descriptor, String mediaType,
			String subType, boolean includeExtensions) {
		composeBodyFields(descriptor, mediaType, subType);
		if ("text".equalsIgnoreCase(mediaType)) {
			message(descriptor.getLines());
		}
		if (includeExtensions) {
			encodeOnePartBodyExtensions(descriptor);
		}
	}
	
	void encodeOnePartBodyExtensions(MimeDescriptor descriptor) {
		nillableQuote(descriptor.getMd5());
		composeDisposition(descriptor);
		nillableQuotes(descriptor.getLanguages());
		nillableQuote(descriptor.getLocation());
	}

	void composeBodyFields(MimeDescriptor descriptor, String mediaType,
			String subType) {
		quoteUpper(mediaType);
		quoteUpper(subType);
		nillableQuotes(descriptor.getParameters());
		nillableQuote(descriptor.getId());
		nillableQuote(descriptor.getDescription());
		quoteUpper(descriptor.getEncoding());
		message(descriptor.getBodyOctets());
	}
	
	void composeDisposition(MimeDescriptor descriptor) {
		String disposition = descriptor.getDisposition();
		if (disposition == null) {
			nil();
		} else {
			openParen("(");
			quote(disposition);
			nillableQuotes(descriptor.getDispositionParams());
			closeParen(")");
		}
	}
	
	void composeBodyContent(FetchResponse response) {
		Content content = response.getContent();
		if (content != null) {
			message(content.getName());
			literal(content);
		}
	}

	void address(Address[] addresses) {
		if (ArrayUtils.isEmpty(addresses)) {
			nil();
		} else {
			openParen("(");
			for (int i = 0; i < addresses.length; i++) {
				skipNextSpace();
				openParen("(");
				nillableQuote(addresses[i].getPersonalName());
				nillableQuote(addresses[i].getAtDomainList());
				nillableQuote(addresses[i].getMailboxName());
				nillableQuote(addresses[i].getHostName());
				closeParen(")");
			}
			closeParen(")");
		}
	}
	
}
