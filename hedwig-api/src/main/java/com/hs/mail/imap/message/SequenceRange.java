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
package com.hs.mail.imap.message;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class SequenceRange {

	private long min;
	private long max;

	public SequenceRange(long num) {
		this.min = num;
		this.max = num;
	}

	public SequenceRange(long min, long max) {
		this.min = min;
		this.max = max;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public boolean includes(long num) {
		return min <= num && num <= max;
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int) (max ^ (max >>> 32));
		result = PRIME * result + (int) (min ^ (min >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		return obj instanceof SequenceRange && equals((SequenceRange) obj);
	}

	public boolean equals(SequenceRange range) {
		return (min == range.min && max == range.max);
	}
	
}
