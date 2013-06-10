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
package com.hs.mail.smtp.message;

import com.hs.mail.smtp.SmtpException;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 15, 2010
 *
 */
public class Recipient extends MailAddress {

	private static final long serialVersionUID = -873948187533142386L;

	private long id = -1;
	
	public Recipient(String address) throws SmtpException {
		super(address);
	}
	
	public Recipient(String address, boolean strict) throws SmtpException {
		super(address, strict);
	}

	public Recipient(long id, String address, boolean strict)
			throws SmtpException {
		super(address, strict);
		setID(id);
	}

	public void setID(long id) {
		this.id = id;
	}

	public long getID() {
		return id;
	}
	
	public String toString() {
		return "(" + id + ") \"" + getMailbox() + "\"";
	}

}
