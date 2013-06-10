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

import org.apache.commons.collections.ListUtils;

/**
 * This class implements the logical AND operator on individual SearchKeys.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public final class AndKey extends SearchKeyList {

	public AndKey() {
		super();
	}

	public AndKey(SearchKey k1, SearchKey k2) {
		super();
		addKey(k1);
		addKey(k2);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AndKey))
			return false;
		return ListUtils.isEqualList(keys, ((AndKey) obj).getSearchKeys());
	}

	public int hashCode() {
		return ListUtils.hashCodeForList(keys);
	}

	@Override
	public boolean isComposite() {
		return true;
	}

}
