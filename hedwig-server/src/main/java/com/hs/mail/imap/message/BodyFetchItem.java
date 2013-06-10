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

import javax.mail.FetchProfile;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 29, 2010
 *
 */
public class BodyFetchItem extends FetchProfile.Item {

	public static final int HEADER_FIELDS_NOT = 0;
	public static final int HEADER_FIELDS = 1;
	public static final int HEADER = 2;
	/**
	 * Text body of the message, omitting the [RFC-2822] header
	 */
	public static final int TEXT = 3;
	/**
	 * [RFC-2045] header for the part
	 */
	public static final int MIME = 4;
	/**
	 * Entire message including the header
	 */
	public static final int CONTENT = 5;

	private String name;
	private boolean peek;	// BODY.PEEK when fetching contents?
	private int sectionType = CONTENT;
	private String[] headers;
	private int[] path;
	private long firstOctet = -1;
	private long numberOfOctets = -1;

	public BodyFetchItem(String name, boolean peek) {
		super(name);
		setName(name);
		this.setPeek(peek);
	}

	public BodyFetchItem(String name, boolean peek, int sectionType) {
		this(name, peek);
		setSectionType(sectionType);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPeek(boolean peek) {
		this.peek = peek;
	}

	public boolean isPeek() {
		return peek;
	}

	public void setSectionType(int sectionType) {
		this.sectionType = sectionType;
	}

	public int getSectionType() {
		return sectionType;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public String[] getHeaders() {
		return headers;
	}
	
	private void ensureSpaceForOneInPath() {
		if (path == null) {
			path = new int[1];
		} else {
			int length = path.length;
			int[] newPath = new int[length + 1];
			System.arraycopy(path, 0, newPath, 0, length);
			path = newPath;
		}
	}
	
	public void addPath(int part) {
		ensureSpaceForOneInPath();
		path[path.length - 1] = part;
	}
	
    public int[] getPath() {
        return path;
    }

	public void setFirstOctet(long firstOctet) {
		this.firstOctet = firstOctet;
	}

	public long getFirstOctet() {
		return firstOctet;
	}

	public void setNumberOfOctets(long numberOfOctets) {
		this.numberOfOctets = numberOfOctets;
	}

	public long getNumberOfOctets() {
		return numberOfOctets;
	}
	
}
