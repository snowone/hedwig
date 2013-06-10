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
package com.hs.mail.imap.message.search;

/**
 * This class implements search-criteria for the Message Header or Contents
 * text.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public final class TextKey extends StringKey {
	
	/**
	 * True if search for header of the message, otherwise false (search for
	 * body of the message)
	 */
	protected boolean header;

	public TextKey(String pattern, boolean header) {
		super(pattern);
		this.header = header;
	}
	
	public TextKey(String pattern) {
		this(pattern, true);
	}

	public boolean getSearchHeader() {
		return header;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof TextKey))
			return false;
		TextKey tk = (TextKey) obj;
		return tk.header == this.header && super.equals(obj);
	}

	@Override
	public boolean isComposite() {
		return false;
	}

}
