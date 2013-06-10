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

import java.util.Date;

/**
 * This class implements search-criteria for Dates.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public abstract class DateKey extends ComparisonKey {

	protected Date date;

	protected DateKey(int comparison, Date date) {
		super(comparison);
		this.date = date;
	}

	public Date getDate() {
		return new Date(date.getTime());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof DateKey))
			return false;
		DateKey dk = (DateKey) obj;
		return dk.date.equals(this.date) && super.equals(obj);
	}

	public int hashCode() {
		return date.hashCode() + super.hashCode();
	}

}
