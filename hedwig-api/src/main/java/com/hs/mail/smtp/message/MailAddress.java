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

import java.io.Serializable;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.ParseException;

import com.hs.mail.smtp.SmtpException;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 1, 2010
 *
 */
public class MailAddress implements Serializable, Comparable {

	private static final long serialVersionUID = -3308891400855394336L;

	private String address;
	private transient boolean strict;
	private transient String user;
	private transient String host;

	public MailAddress() {
		this.address = "";
	}
	
	public MailAddress(Address address) {
		this.address = ((InternetAddress) address).getAddress();
	}

	public MailAddress(String address, boolean strict) throws SmtpException {
		this.strict = strict;
		if (strict) {
			try {
				this.address = parse(address);
			} catch (ParseException e) {
				throw new SmtpException("501 5.1.7 " + e.getMessage());
			}
		} else {
			this.address = address;
		}
	}
	
	public MailAddress(String address) throws SmtpException {
		this(address, true);
	}
	
	public boolean isStrict() {
		return strict;
	}

	public String getMailbox() {
		return address;
	}
	
	protected void setMailbox(String address) {
		this.address = address;
	}

	public String getUser() {
		if (user == null) {
			int index = address.lastIndexOf('@');
			user = (index != -1) ? address.substring(0, index) : address;
		}
		return user;
	}

    public String getHost() {
		if (host == null) {
			int index = address.lastIndexOf('@');
			if (index != -1) {
				host = address.substring(index + 1).toLowerCase();
				if (host.charAt(0) == '[')
					host = host.substring(1, host.length() - 1);
			}
		}
		return host;
	}
	
	public InternetAddress toInternetAddress() {
		try {
			return new InternetAddress(address);
		} catch (javax.mail.internet.AddressException ae) {
			// impossible really
			return null;
		}
	}
	
	/**
	 * Strip source routing, according to RFC-2821 it is an allowed approach to
	 * handle mails containing RFC-821 source-route information.
	 */
	private static String stripSourceRoute(String address) {
		String s = address;
		int index = address.indexOf(':');
		if (index != -1) {
			s = address.substring(index + 1);
		}
		return s;
	}

	private static String parse(String address) throws ParseException {
		String s = address;
		if (!s.startsWith("<") || !s.endsWith(">")) {
			throw new ParseException("Address did not start and end with < >");
		}
		// Strip < and >
		s = s.substring(1, s.length() - 1);
		// Test if mail address has source routing information (RFC-821) and
		// get rid of it!!
		s = stripSourceRoute(s);
		// When the strict flag is set true, InternetAddress checks that the
		// address is a valid "mailbox" per RFC822.
		new InternetAddress(s, true);
		return s;
	}

	public int compareTo(Object o) {
		if (o instanceof MailAddress)
			return address.compareTo(((MailAddress) o).getMailbox());
		else if (o instanceof InternetAddress)
			return address.compareTo(((InternetAddress) o).getAddress());
		else if (o instanceof String)
			return address.compareTo((String) o);
		else
			return -1;
	}

}
