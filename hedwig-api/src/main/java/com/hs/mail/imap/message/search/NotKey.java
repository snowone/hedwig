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
 * This class implements the logical NEGATION operator.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public final class NotKey extends SearchKey {

	protected SearchKey key;

	public NotKey(SearchKey k) {
		key = k;
	}

	public SearchKey getSearchKey() {
		return key;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof NotKey))
			return false;
		NotKey nk = (NotKey) obj;
		return nk.key.equals(this.key);
	}

	public int hashCode() {
		return key.hashCode() << 1;
	}

	@Override
	public boolean isComposite() {
		return true;
	}

}
