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
package com.hs.mail.imap.message.request;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.ImapSession.State;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class AuthenticateRequest extends ImapRequest {

	/**
	 * Authentication mechanism name
	 */
	private final String authType;

	public AuthenticateRequest(String tag, String command, String authType) {
		super(tag, command);
		this.authType = authType;
	}

	public String getAuthType() {
		return authType;
	}

	@Override
	public boolean validForState(State state) {
		return state == ImapSession.State.NON_AUTHENTICATED;
	}

}
