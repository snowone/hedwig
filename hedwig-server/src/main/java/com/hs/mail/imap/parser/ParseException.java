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
package com.hs.mail.imap.parser;

import java.util.LinkedList;

import org.apache.commons.collections.CollectionUtils;

/**
 * This exception is thrown when errors are encountered while parsing the IMAP
 * command.
 * 
 * @author Won Chul Doh
 * @since Jan 12, 2010
 * 
 */
public class ParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String tag;

	public ParseException(String tag, Throwable cause) {
		super(cause);
		this.tag = tag;
	}

	public ParseException(String tag, String message) {
		super(message);
		this.tag = tag;
	}

	public ParseException(LinkedList<Token> tokens, String message) {
		this(tokens, message, null);
	}

	public ParseException(LinkedList<Token> tokens, String message,
			Throwable cause) {
		super(message, cause);
		this.tag = CollectionUtils.isNotEmpty(tokens) ? tokens.getFirst().value
				: "*";
	}

	public String getTag() {
		return tag;
	}
	
	public String getMessage() {
		return new StringBuffer(tag).append(" BAD ").append(super.getMessage())
				.toString();
	}

}
