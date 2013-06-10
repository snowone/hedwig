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
package com.hs.mail.imap.mailbox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Expresses select criteria for mailboxes.
 * 
 * @author Won Chul Doh
 * @since Mar 9, 2010
 * 
 */
public class MailboxQuery {

	private String expression;
	private Matcher matcher;

	public MailboxQuery(String referenceName, String mailboxName) {
		this.expression = getExpression(referenceName, mailboxName,
				Mailbox.folderSeparator);
		if (expression.indexOf('*') >= 0 || expression.indexOf('%') >= 0) {
			this.matcher = createMatcher(expression);
		}
	}
	
	private static String getExpression(String referenceName,
			String mailboxName, String sep) {
		StringBuilder sb = new StringBuilder(referenceName);
		if (mailboxName.startsWith(sep)) {
			sb.append(mailboxName.substring(sep.length()));
		} else if (StringUtils.isEmpty(referenceName)) {
			sb.append(mailboxName);
		} else {
			sb.append(sep).append(mailboxName);
		}
		return sb.toString();
	}
	
	public String getExpression() {
		return expression;
	}

	public boolean containsWildcard() {
		return (matcher != null);
	}
	
	private static Matcher createMatcher(String expression) {
		StringBuilder sb = new StringBuilder(expression.length());
		boolean quoted = false;
		for (int i = 0, n = expression.length(); i < n; i++) {
			char c = expression.charAt(i);
			if (c == '*' || c == '%') {
				if (quoted) {
					sb.append("\\E");
					quoted = false;
				}
				sb.append((c == '*') ? ".*" : "[^\\.]*");
			} else {
				if (!quoted) {
					sb.append("\\Q");
					quoted = true;
				}
				sb.append(c);
			}
		}
		if (quoted) {
			sb.append("\\E");
		}
		Pattern p = Pattern.compile(sb.toString());
		return p.matcher("");
	}

	/**
	 * Is the given name a match for {@link #expression}
	 * 
	 * @param name
	 *            name to be matched
	 * @return true if the given name matches this expression, false otherwise
	 */
	public boolean match(String name) {
		if (matcher != null) {
			matcher.reset(name);
			return matcher.matches();
		} else {
			return expression.equals(name);
		}
	}

}
