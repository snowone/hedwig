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
package com.hs.mail.imap.server.codec;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 22, 2010
 *
 */
public class ImapCodecUtil {
	/**
	 * Carriage return
	 */
	static final byte CR = 13;

	/**
	 * Line feed character
	 */
	static final byte LF = 10;

	/**
	 * carriage return line feed
	 */
	static final char[] CRLF = new char[] { '\r', '\n' };
	
	private ImapCodecUtil() {
		super();
	}

}
