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

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 20, 2010
 *
 */
public class AbstractImapCommandParser {
	
	protected StringReader reader;
	protected char pushback[];
	protected StringBuffer buffer;
	protected int pos;
	protected LinkedList<Token> tokens;

	public AbstractImapCommandParser(StringReader in) {
		reader = in;
		pushback = new char[32];
		buffer = new StringBuffer();
		pos = -1;
		tokens = new LinkedList<Token>();
	}

    /******************************************
     * THE COMMAND GRAMMAR STARTS HERE        *
     ******************************************/
	
    protected boolean astring() {
		if (astring_char(read())) {
			while (astring_char(read()))
				;
			unread();
			newToken(Token.Type.ASTRING);
			return true;
		} else {
			unread();
			return string();
		}
	}

    protected boolean astring_char(char c) {
		return atom_char(c) || resp_special(c);
	}

    protected boolean atom() {
		if (atom_char(read())) {
			while (atom_char(read()))
				;
			unread();
			newToken(Token.Type.ATOM);
			return true;
		} else {
			unread();
			return false;
		}
	}

    protected boolean atom_char(char c) {
		return !atom_specials(c);
	}
	
    protected boolean atom_specials(char c) {
/*		return c == '(' || c == ')' || c == '{' || c == ' ' || c <= '\037'
				|| c >= '\177' || list_wildcard(c) || quoted_special(c)
				|| resp_special(c); 
*/		return c == '(' || c == ')' || c == '{' || c == ' ' || c < 32
			|| c == 127 || list_wildcard(c) || quoted_special(c)
			|| resp_special(c); 
	}

    protected boolean date() {
		boolean dquote = false;
		if (read() == '"')
			dquote = true;
		else
			unread();
		if (!date_text()) {
			unreadAll();
			return false;
		}
		if (dquote && read() != '"') {
			unreadAll();
			return false;
		}
		if (dquote)
			undquote();
		newToken(Token.Type.DATE);
		return true;
	}
    
    private boolean date_day() {
    	return _number(2) || _number(1);
    }

    private boolean date_day_fixed() {
		return (_sp() && _number(1)) || _number(2); 
	}
    
    private boolean date_month() {
		return _kw("Jan") || _kw("Feb") || _kw("Mar") || _kw("Apr")
				|| _kw("May") || _kw("Jun") || _kw("Jul") || _kw("Aug")
				|| _kw("Sep") || _kw("Oct") || _kw("Nov") || _kw("Dec");
	}
    
    private boolean date_text() {
		return date_day() && _kw("-") && date_month() && _kw("-")
				&& date_year();
	}
    
    private boolean date_year() {
		return _number(4);
	}
    
    protected boolean date_time() {
		if (read() != '"') {
			unread();
			return false;
		}
		if (!date_day_fixed() || !_kw("-") || !date_month() || !_kw("-")
				|| !date_year() || !_sp() || !time() || !_sp() || !zone()) {
			unreadAll();
			return false;
		}
		if (read() != '"') {
			unreadAll();
			return false;
		} else {
			undquote();
			newToken(Token.Type.DATE_TIME);
			return true;
		}
	}

    protected boolean list_char(char c) {
		return atom_char(c) || list_wildcard(c) || resp_special(c);
	}

    protected boolean list_wildcard(char c) {
		return c == '*' || c == '%';
	}

    protected boolean literal() {
		if (read() != '{') {
			unread();
			return false;
		}
		if (!_number()) {
			unreadAll();
			return false;
		}
		boolean sync = false;
		if (read() != '+') {
			unread();
		} else {
			buffer.setLength(buffer.length() - 1);
			sync = true;
		}
		if (read() != '}') {
			unreadAll();
			return false;
		}
		int length = Integer.parseInt(buffer.substring(1, buffer.length() - 1));
		buffer.setLength(0);
		buffer.append(length);
		newToken((sync) ? Token.Type.LITERAL_SYNC : Token.Type.LITERAL);
		return true;
	}

    protected boolean number() {
		return number(0, true);
	}

    protected boolean _number() {
		return number(0, false);
	}

    protected boolean number(int digits) {
		return number(digits, true);
	}

    protected boolean _number(int digits) {
		return number(digits, false);
	}

	private boolean number(int digits, boolean tokenize) {
		if (digit(read())) {
			int count;
			for (count = 1; digit(read()); count++)
				;
			unread();
			if (digits > 0 && count != digits) {
				for (int j = count; j > 0; j--)
					unread();

				return false;
			}
			if (tokenize)
				newToken(Token.Type.NUMBER);
			return true;
		} else {
			unread();
			return false;
		}
	}
	
	protected boolean nz_number() {
		if (nz_digit(read())) {
			while (digit(read()))
				;
			unread();
			newToken(Token.Type.NZ_NUMBER);
			return true;
		} else {
			unread();
			return false;
		}
	}

    protected boolean quoted() {
		if (read() != '"') {
			unread();
			return false;
		}
		while (quoted_char(read()))
			;
		unread();
		if (read() != '"') {
			unreadAll();
			return false;
		} else {
			unEscape();
			undquote();
			newToken(Token.Type.QUOTED);
			return true;
		}
	}

    protected boolean quoted_char(char c) {
		if (c == '\\') {
			char _c = read();
			if (_c != '"' && _c != '\\') {
				unread();
				return false;
			} else {
				return true;
			}
		} else {
			return text_char(c) && !quoted_special(c);
		}
	}

    protected boolean quoted_special(char c) {
		return c == '"' || c == '\\';
	}

    protected boolean resp_special(char c) {
		return c == ']';
	}

    private boolean seq_number() {
		return seq_number(true);
	}

	private boolean _seq_number() {
		return seq_number(false);
	}

	private boolean seq_number(boolean tokenize) {
		if (!_number() && !_kw("*"))
			return false;
		if (tokenize)
			newToken(Token.Type.SEQ_NUMBER);
		return true;
	}

	private boolean seq_range() {
		if (!_seq_number() || !_kw(":") || !_seq_number()) {
			unreadAll();
			return false;
		} else {
			newToken(Token.Type.SEQ_RANGE);
			return true;
		}
	}
    
    protected boolean sequence_set() {
		if (!seq_range() && !seq_number())
			return false;
		if (kw(","))
			do
				if (!sequence_set())
					return false;
			while (kw(","));
		return true;
	}

    protected boolean string() {
		return quoted() || literal();
	}

    private boolean time() {
		return _number(2) && _kw(":") && _number(2) && _kw(":") && _number(2);
	}
    
    private boolean zone() {
		return (_kw("+") || _kw("-")) && _number(4);
	}

    /******************************************
     * THE COMMAND GRAMMAR ENDS HERE          *
     ******************************************/
    
    protected boolean crlf() {
		if (read() != '\r') {
			unread();
			return false;
		}
		if (read() != '\n') {
			unread();
			return false;
		} else {
			buffer.setLength(0);
			return true;
		}
	}
    
    private boolean digit(char c) {
		return c >= '0' && c <= '9';
	}
    
	protected boolean kw(String pattern) {
		return kw(pattern, true);
	}

	protected boolean _kw(String pattern) {
		return kw(pattern, false);
	}

	private boolean kw(String pattern, boolean tokenize) {
		pattern = pattern.toUpperCase();
		int i = 0;
		for (int n = pattern.length(); i < n; i++) {
			char c = read();
			if (Character.toUpperCase(c) != pattern.charAt(i)) {
				for (int j = i; j >= 0; j--)
					unread();

				return false;
			}
		}

		if (tokenize)
			newToken(Token.Type.KEYWORD);
		return true;
	}
	
	protected boolean lparen() {
		if (read() == '(') {
			newToken(Token.Type.LPAREN);
			return true;
		} else {
			unread();
			return false;
		}
	}

    private boolean nz_digit(char c) {
		return c >= '1' && c <= '9';
	}

    protected boolean rparen() {
		if (read() == ')') {
			newToken(Token.Type.RPAREN);
			return true;
		} else {
			unread();
			return false;
		}
	}

	protected boolean sp() {
		return sp(true);
	}

	protected boolean _sp() {
		return sp(false);
	}
	
	private boolean sp(boolean tokenize) {
		if (read() == ' ') {
			if (tokenize)
				buffer.setLength(0);
			return true;
		} else {
			unread();
			return false;
		}
	}

	protected boolean tag_char(char c) {
		return astring_char(c) && c != '+';
	}

	private boolean text_char(char c) {
		return c >= '\001' && c <= '\377' && c != '\r' && c != '\n';
	}

	protected char read() {
		char c;
		if (pos >= 0) {
			c = pushback[pos--];
		} else {
			try {
				int i = reader.read();
				if (i == -1)
					throw new ParseException(tokens,
							"Unexpected end of stream");
				c = (char) i;
			} catch (IOException e) {
				throw new ParseException(tokens,
						"Error while reading character", e);
			}
		}
		buffer.append(c);
		return c;
	}

	protected void unread() {
		int len = buffer.length();
		if (pos == pushback.length) {
			throw new ParseException(tokens, "Pushback buffer too short");
		} else {
			pushback[++pos] = buffer.charAt(len - 1);
			buffer.setLength(len - 1);
			return;
		}
	}

	protected void unreadAll() {
		for (; buffer.length() > 0; unread())
			;
	}
	
	private void unEscape() {
		for (int i = 1; i < buffer.length() - 1; i++) {
			char c = buffer.charAt(i);
			if (c == '\\')
				buffer.deleteCharAt(i);
		}
	}

	private void undquote() {
		buffer.deleteCharAt(0);
		buffer.deleteCharAt(buffer.length() - 1);
	}
	
	protected void newToken(Token.Type type) {
		if (buffer.length() > 1024)
			throw new ParseException(tokens,
					"Command line length exceeds fixed limit");
		String value = buffer.toString();
		Token token = new Token(type, value);
		tokens.add(token);
		buffer.setLength(0);
	}
	
}
