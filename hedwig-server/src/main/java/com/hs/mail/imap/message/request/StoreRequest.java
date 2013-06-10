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
package com.hs.mail.imap.message.request;

import javax.mail.Flags;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.ImapSession.State;
import com.hs.mail.imap.message.SequenceRange;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class StoreRequest extends ImapRequest {

	private final SequenceRange[] sequenceSet;
    private final boolean minus;
    private final boolean plus;
    private final boolean silent;
    private final Flags flags;
	private final boolean useUID;

	public StoreRequest(String tag, String command,
			SequenceRange[] sequenceSet, Boolean sign, final boolean silent,
			final Flags flags, boolean useUID) {
		super(tag, command);
		this.sequenceSet = sequenceSet;
		this.minus = (sign != null && !sign.booleanValue());
		this.plus = (sign != null && sign.booleanValue());
		this.silent = silent;
		this.flags = flags;
		this.useUID = useUID;
	}

	public SequenceRange[] getSequenceSet() {
		return sequenceSet;
	}

	public boolean isMinus() {
		return minus;
	}

	public boolean isPlus() {
		return plus;
	}
	
	public boolean isReplace() {
		return !plus && !minus;
	}

	public boolean isSilent() {
		return silent;
	}

	public Flags getFlags() {
		return flags;
	}

	public boolean isUseUID() {
		return useUID;
	}

	@Override
	public boolean validForState(State state) {
		return state == ImapSession.State.SELECTED;
	}

}
