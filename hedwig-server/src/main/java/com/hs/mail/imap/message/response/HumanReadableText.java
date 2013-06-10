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

/**
 * 
 * @author Won Chul Doh
 * @since Mar 19, 2010
 *
 */
public interface HumanReadableText {
	
	public static final String GREETINGS = "* OK Hedwig ready.\r\n";

	public static final String BYE = "IMAP4rev1 Server logging out.";

	public static final String FAILED_TO_CREATE_INBOX = "Cannot create special mailbox INBOX.";

	public static final String FAILED_TO_DELETE_INBOX = "Cannot delete special mailbox INBOX.";
	
	public static final String INVADE_STRUCTURE = "New mailbox would invade mailbox structure.";

	public static final String INVALID_COMMAND = "Command not valid in this state.";

	public static final String MAILBOX_DELETED_SIGN_OFF = "Selected mailbox has been deleted by another session.";
	
	public static final String MAILBOX_EXISTS = "Mailbox already exists.";
	
	public static final String MAILBOX_IS_READ_ONLY = "Mailbox is read only.";

	public static final String MAILBOX_NOT_FOUND = "No such mailbox.";
	
	public static final String MAILBOX_NOT_SELECTABLE = "Mailbox is not selectable.";

	public static final String NAMESPACE_NOT_EXIST = "No such namespace";
	
	public static final String NO_SUCH_QUOTA_ROOT = "No such quota root.";

	public static final String UNSUPPORTED_AUTHENTICATION_MECHANISM = "Authentication mechanism is unsupported.";
	

}
