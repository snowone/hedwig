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
 * This class models the comparison operator. This is an abstract class;
 * subclasses define different comparison for its datatypes.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public abstract class ComparisonKey extends SearchKey {

    public static final int LE = 1;
    public static final int LT = 2;
    public static final int EQ = 3;
    public static final int NE = 4;
    public static final int GT = 5;
    public static final int GE = 6;

    protected int comparison;

	protected ComparisonKey(int comparison) {
		this.comparison = comparison;
	}

	public int getComparison() {
		return comparison;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ComparisonKey))
			return false;
		ComparisonKey ck = (ComparisonKey) obj;
		return ck.comparison == this.comparison;
	}

	public int hashCode() {
		return comparison;
	}

}
