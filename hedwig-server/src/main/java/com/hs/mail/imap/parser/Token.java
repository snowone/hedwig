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

/**
 * 
 * @author Won Chul Doh
 * @since Jan 12, 2010
 *
 */
public class Token {
	
	public static enum Type {
		ASTRING, ATOM, CRLF, DATE, DATE_TIME, KEYWORD, LIST_MAILBOX, LITERAL, LITERAL_SYNC, LPAREN, MAILBOX, NSTRING, NUMBER, NZ_NUMBER, QUOTED, RPAREN, SEQ_NUMBER, SEQ_RANGE, SP, STRING, TAG
	};

	public Type type;
	public String value;
	public Token next;

	public Token(Type type, String value) {
        this.type = type;
        this.value = value;
	}
	
	public boolean isLiteral() {
		return (Type.LITERAL == type || Type.LITERAL_SYNC == type);
	}
	
	public String toString() {
		return type + "=\"" + value + "\"";
	}

}
