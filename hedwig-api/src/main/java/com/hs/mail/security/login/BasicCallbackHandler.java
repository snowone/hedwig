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
package com.hs.mail.security.login;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Basic JAAS username password CallbackHandler
 * 
 * @author Won Chul Doh
 * @since Jul 18, 2007
 *
 */
public class BasicCallbackHandler implements CallbackHandler {

	private String username;
	private char[] password;

	public BasicCallbackHandler(String username, char[] password) {
		this.username = username;
		this.password = password;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			Callback callback = callbacks[i];
			if (callback instanceof NameCallback)
				((NameCallback) callback).setName(username);
			else if (callback instanceof PasswordCallback)
				((PasswordCallback) callback).setPassword(password);
			else
				throw new UnsupportedCallbackException(callback,
						"Callback class not supported");
		}
	}

}
