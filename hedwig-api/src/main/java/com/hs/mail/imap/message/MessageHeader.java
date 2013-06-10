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
package com.hs.mail.imap.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.field.AddressListField;
import org.apache.james.mime4j.field.DateTimeField;
import org.apache.james.mime4j.field.FieldName;
import org.apache.james.mime4j.field.MailboxListField;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.io.MaxHeaderLimitException;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.springframework.util.CollectionUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class MessageHeader {
	private Header header =  new Header();

	public MessageHeader(InputStream is) throws MimeIOException, IOException {
		final MimeStreamParser parser = createMimeParser();
		parser.setContentHandler(new AbstractContentHandler() {
			@Override
			public void endHeader() {
				parser.stop();
			}
			@Override
			public void field(Field field) throws MimeException {
				Field parsedField = AbstractField.parse(field.getRaw());
				header.addField(parsedField);
			}
		});
        try {
        	parser.parse(is);
        } catch (MaxHeaderLimitException ex) {
        	// Ignore this exception
        } catch (MimeException ex) {
        	throw new MimeIOException(ex);
        }
	}
	
	private static MimeStreamParser createMimeParser() {
		MimeEntityConfig config = new MimeEntityConfig();
		config.setMaxLineLen(-1);
		return new MimeStreamParser(config);
	}
	
    public Header getHeader() {
		return header;
	}

    public String getSubject() {
		UnstructuredField field = obtainField(FieldName.SUBJECT);
		if (field == null)
			return null;

		return field.getValue();
	}

    public Date getDate() {
		DateTimeField dateField = obtainField(FieldName.DATE);
		if (dateField == null)
			return null;

		return dateField.getDate();
	}

	public Mailbox getFrom() {
		MailboxList mailboxList = getMailboxList(FieldName.FROM);
		if (CollectionUtils.isEmpty(mailboxList)) {
			Field field = header.getField(FieldName.FROM);
			return (field != null) ? new Mailbox(field.getBody(), null) : null;
		} else {
			return mailboxList.get(0);
		}
	}
	
	public Mailbox getReplyTo() {
		MailboxList mailboxList = getMailboxList(FieldName.REPLY_TO);
		if (CollectionUtils.isEmpty(mailboxList)) {
			return null;
		} else {
			return mailboxList.get(0);
		}
	}

    public AddressList getTo() {
		return getAddressList(FieldName.TO);
	}

    public AddressList getCc() {
        return getAddressList(FieldName.CC);
    }

    public AddressList getBcc() {
        return getAddressList(FieldName.BCC);
    }

    private MailboxList getMailboxList(String fieldName) {
        MailboxListField field = obtainField(fieldName);
        if (field == null)
            return null;

        return field.getMailboxList();
    }

    private AddressList getAddressList(String fieldName) {
		AddressListField field = obtainField(fieldName);
		if (field == null)
			return null;

		return field.getAddressList();
	}
    
	<F extends Field> F obtainField(String fieldName) {
		if (header == null)
			return null;

		@SuppressWarnings("unchecked")
		F field = (F) header.getField(fieldName);
		return field;
	}
	
}
