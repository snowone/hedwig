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

import javax.mail.FetchProfile;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.ImapSession.State;
import com.hs.mail.imap.message.SequenceRange;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class FetchRequest extends ImapRequest {

	private final SequenceRange[] sequenceSet;
	private final FetchProfile fetchProfile;
	private final boolean useUID;
	
	public FetchRequest(String tag, String command,
			SequenceRange[] sequenceSet, FetchProfile fetchProfile,
			boolean useUID) {
		super(tag, command);
		this.sequenceSet = sequenceSet;
		this.fetchProfile = fetchProfile;
		this.useUID = useUID;
	}

	public SequenceRange[] getSequenceSet() {
		return sequenceSet;
	}

	public FetchProfile getFetchProfile() {
		return fetchProfile;
	}

	public boolean isUseUID() {
		return useUID;
	}

	@Override
	public boolean validForState(State state) {
		return state == ImapSession.State.SELECTED;
	}

}
