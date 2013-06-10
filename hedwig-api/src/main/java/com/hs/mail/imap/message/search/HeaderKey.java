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
 * This class implements search-criteria for Message headers.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public final class HeaderKey extends StringKey {

    protected String headerName;

	public HeaderKey(String headerName, String pattern) {
		super(pattern);
		this.headerName = headerName;
	}

    public String getHeaderName() {
		return headerName;
	}

    public boolean equals(Object obj) {
		if (!(obj instanceof HeaderKey))
			return false;
		HeaderKey hk = (HeaderKey) obj;
		return hk.headerName.equalsIgnoreCase(headerName) && super.equals(hk);
	}

	public int hashCode() {
		return headerName.toLowerCase().hashCode() + super.hashCode();
	}

	@Override
	public boolean isComposite() {
		return true;
	}

}
