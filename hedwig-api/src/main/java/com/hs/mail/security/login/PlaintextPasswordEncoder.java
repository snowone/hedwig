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

/**
 * Default password encoder for the case where no password encoder is needed.
 * Encoding results in the same password that was passed in.
 * 
 * @author Won Chul Doh
 * @since Jul 18, 2007
 * 
 */
public class PlaintextPasswordEncoder implements PasswordEncoder {

	public PlaintextPasswordEncoder() {
	}
	
	public String encode(char[] password) {
		return new String(password);
	}

	public boolean compare(String encoded, char[] plain) {
        return encoded.equals(encode(plain));
	}

}
