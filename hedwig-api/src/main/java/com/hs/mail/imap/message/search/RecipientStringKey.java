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
package com.hs.mail.imap.message.search;

import javax.mail.Message;

/**
 * This class implements search-criteria for the Recipient Address header. <p>
 *
 * @author Won Chul Doh
 * @since Jan 30, 2010
 *
 */
public final class RecipientStringKey extends AddressStringKey {

	private Message.RecipientType type;

	public RecipientStringKey(Message.RecipientType type, String pattern) {
		super(pattern);
		this.type = type;
	}

	public Message.RecipientType getRecipientType() {
		return type;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof RecipientStringKey))
			return false;
		RecipientStringKey rsk = (RecipientStringKey) obj;
		return rsk.type.equals(this.type) && super.equals(obj);
	}

	public int hashCode() {
		return type.hashCode() + super.hashCode();
	}

	@Override
	public boolean isComposite() {
		return true;
	}

}
