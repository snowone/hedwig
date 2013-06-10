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
 * This class implements search-criteria for Strings.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public abstract class StringKey extends SearchKey {

	/**
	 * The pattern.
	 */
    protected String pattern;
    
    /**
     * Ignore case when comparing?
     */
    protected boolean ignoreCase;

    protected StringKey(String pattern) {
		this.pattern = pattern;
		ignoreCase = true;
	}

    protected StringKey(String pattern, boolean ignoreCase) {
		this.pattern = pattern;
		this.ignoreCase = ignoreCase;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean getIgnoreCase() {
		return ignoreCase;
	}
 
    public boolean equals(Object obj) {
		if (!(obj instanceof StringKey))
			return false;
		StringKey sk = (StringKey) obj;
		if (ignoreCase)
			return sk.pattern.equalsIgnoreCase(this.pattern)
					&& sk.ignoreCase == this.ignoreCase;
		else
			return sk.pattern.equals(this.pattern)
					&& sk.ignoreCase == this.ignoreCase;
	}
	
    public int hashCode() {
		return ignoreCase ? pattern.hashCode() : ~pattern.hashCode();
	}
	
}
