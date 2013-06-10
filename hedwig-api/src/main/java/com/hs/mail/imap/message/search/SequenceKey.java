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

import org.apache.commons.lang.ArrayUtils;

import com.hs.mail.imap.message.SequenceRange;

/**
 * This class implements search-criteria for the message sequence numbers.
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public final class SequenceKey extends SearchKey {

	protected SequenceRange[] sequenceSet;
	protected boolean useUid;
	
	public SequenceKey(SequenceRange[] sequenceSet, boolean useUid) {
		this.sequenceSet = sequenceSet;
		this.useUid = useUid;
	}

	public SequenceKey(SequenceRange[] sequenceSet) {
		this(sequenceSet, false);
	}

	public SequenceRange[] getSequenceSet() {
		return sequenceSet;
	}
	
    public boolean isUseUid() {
		return useUid;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SequenceKey))
			return false;
		SequenceKey sk = (SequenceKey) obj;
		return sk.useUid == this.useUid
				&& ArrayUtils.isEquals(sk.sequenceSet, this.sequenceSet);
	}

	@Override
	public boolean isComposite() {
		return true;
	}

}
