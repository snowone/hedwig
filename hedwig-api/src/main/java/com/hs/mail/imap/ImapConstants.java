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
package com.hs.mail.imap;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public interface ImapConstants {

	public static final String INBOX_NAME = "INBOX";
	
	public static final String NAMESPACE_PREFIX = "#";

	/**
	 * Common header field names
	 */
	public static final String RFC822_BCC = "Bcc";

	public static final String RFC822_CC = "Cc";

	public static final String RFC822_FROM = "From";

	public static final String RFC822_DATE = "Date";

	public static final String RFC822_SUBJECT = "Subject";

	public static final String RFC822_TO = "To";

	public static final String RFC822_SENDER = "Sender";

	public static final String RFC822_REPLY_TO = "Reply-To";

	public static final String RFC822_IN_REPLY_TO = "In-Reply-To";

	public static final String RFC822_MESSAGE_ID = "Message-ID";
	
	public static final String RFC822_RETURN_PATH = "Return-Path";
    
    /**
     * Capabilities
     */
	public static final String VERSION = "IMAP4rev1";

	public static final String ACL = "ACL";	// RFC4314

	public static final String CHILDREN = "CHILDREN";	// RFC3348
	
	public static final String LITERAL_PLUS = "LITERAL+";	// RFC2088
	
	public static final String LOGINDISABLED = "LOGINDISABLED";	// RC2595, RFC3501
	
	public static final String NAMESPACE = "NAMESPACE";	// RFC2342
	
	public static final String QUOTA = "QUOTA";	// RFC2087

	public static final String SORT = "SORT";	// RFC5256
	
	public static final String STARTTLS = "STARTTLS";	// RFC2595, RFC3501

	public static final String UIDPLUS = "UIDPLUS";	// RFC4315

}
