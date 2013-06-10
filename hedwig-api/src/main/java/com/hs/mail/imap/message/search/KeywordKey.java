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
 * This class implements search-criteria for Message Keyword (User Flag).
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public final class KeywordKey extends StringKey {

	/**
	 * Indicates whether to test for the presence or absence of the specified
	 * keyword. If <code>true</code>, then test whether the specified keyword is
	 * present, else test whether the specified keyword is absent.
	 */
	protected boolean set;

	public KeywordKey(String keyword, boolean set) {
		super(keyword, false);
		this.set = set;
	}

	public boolean getTestSet() {
		return set;
	}

    public boolean equals(Object obj) {
		if (!(obj instanceof KeywordKey))
			return false;
		KeywordKey kk = (KeywordKey) obj;
		return kk.set == this.set && super.equals(obj);
	}

	@Override
	public boolean isComposite() {
		return true;
	}

}
