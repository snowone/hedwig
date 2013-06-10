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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.DomainList;
import org.apache.james.mime4j.field.address.Group;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.field.address.parser.ParseException;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.hs.mail.imap.ImapConstants;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class EnvelopeBuilder {
	
	private static Logger logger = Logger.getLogger(EnvelopeBuilder.class);
	
	public static final String[] WANTED_FIELDS = new String[] {
			ImapConstants.RFC822_DATE, ImapConstants.RFC822_SUBJECT,
			ImapConstants.RFC822_FROM, ImapConstants.RFC822_SENDER,
			ImapConstants.RFC822_REPLY_TO, ImapConstants.RFC822_TO,
			ImapConstants.RFC822_CC, ImapConstants.RFC822_BCC,
			ImapConstants.RFC822_IN_REPLY_TO, ImapConstants.RFC822_MESSAGE_ID };
	
	public Envelope build(Map<String, String> header) {
		String date = header.get(ImapConstants.RFC822_DATE);
		String subject = header.get(ImapConstants.RFC822_SUBJECT);
		Address[] fromAddresses = buildAddresses(header
				.get(ImapConstants.RFC822_FROM));
		Address[] senderAddresses = buildAddresses(header
				.get(ImapConstants.RFC822_SENDER), fromAddresses);
		Address[] replyToAddresses = buildAddresses(header
				.get(ImapConstants.RFC822_REPLY_TO), fromAddresses);
		Address[] toAddresses = buildAddresses(header
				.get(ImapConstants.RFC822_TO));
		Address[] ccAddresses = buildAddresses(header
				.get(ImapConstants.RFC822_CC));
		Address[] bccAddresses = buildAddresses(header
				.get(ImapConstants.RFC822_BCC)); 
		String inReplyTo = header.get(ImapConstants.RFC822_IN_REPLY_TO);
		String messageId = header.get(ImapConstants.RFC822_MESSAGE_ID);
		Envelope envelope = new Envelope(date, subject, fromAddresses,
				senderAddresses, replyToAddresses, toAddresses, ccAddresses,
				bccAddresses, inReplyTo, messageId);
		return envelope;
	}
	
	private Address[] buildAddresses(String value, Address[] defaults) {
		Address[] addresses = buildAddresses(value);
		return (null == addresses) ? defaults : addresses;
	}
	
	private Address[] buildAddresses(String value) {
		try {
			return buildMailAddresses(value);
		} catch (ParseException ex) {
			logger.warn(ex.getMessage());
			return null;
		}
	}
	
	private Address[] buildMailAddresses(String value) throws ParseException {
		if (StringUtils.isEmpty(value)) {
			return null;
		} else {
			AddressList addressList = AddressList.parse(value);
			int size = addressList.size();
			List<Address> addresses = new ArrayList<Address>(size);
			for (int i = 0; i < size; i++) {
				org.apache.james.mime4j.field.address.Address address = addressList
						.get(i);
				if (address instanceof Group) {
					addAddresses((Group) address, addresses);
				} else if (address instanceof Mailbox) {
					Address mailboxAddress = buildAddress((Mailbox) address);
					addresses.add(mailboxAddress);
				} else {
					logger.warn("Unknown address type");
				}
			}
			return addresses.toArray(Address.EMPTY);
		}
	}
	
	private void addAddresses(Group group, List<Address> addresses) {
		String groupName = group.getName();
		addresses.add(new Address(null, null, groupName, null));
		MailboxList mailboxList = group.getMailboxes();
		for (int i = 0; i < mailboxList.size(); i++) {
			Address mailboxAddress = buildAddress(mailboxList.get(i));
			addresses.add(mailboxAddress);
		}
		addresses.add(new Address(null, null, null, null));
	}
	
	private Address buildAddress(Mailbox mailbox) {
		// Javamail raises exception when personal name is surrounded with
		// double quotation mark.
		String name = StringUtils.strip(mailbox.getName(), "\"");
		String domain = mailbox.getDomain();
		DomainList route = mailbox.getRoute();
		String atDomainList;
		if (CollectionUtils.isEmpty(route)) {
			atDomainList = null;
		} else {
			atDomainList = route.toRouteString();
		}
		String localPart = mailbox.getLocalPart();
		return new Address(atDomainList, domain, localPart, name);
	}

}
