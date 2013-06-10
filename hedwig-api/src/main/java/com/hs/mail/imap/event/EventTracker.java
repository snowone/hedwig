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
package com.hs.mail.imap.event;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;

import com.hs.mail.imap.dao.DaoFactory;


/**
 * 
 * @author Won Chul Doh
 * @since Jul 30, 2010
 *
 */
public class EventTracker implements EventListener {

	private boolean deletedMailbox = false;
    private boolean sizeChanged = false;
    private Set<Long> expungedUids;
    private Set<Long> flagUpdatedUids;
    private List<Long> cachedUids;

	public boolean isDeletedMailbox() {
		return deletedMailbox;
	}

	public boolean isSizeChanged() {
		return sizeChanged;
	}

	public boolean hasExpungedUids() {
		return CollectionUtils.isNotEmpty(expungedUids);
	}
	
	public Set<Long> getExpungedUids() {
		return expungedUids;
	}
	
	public boolean hasUpdatedFlags() {
		return CollectionUtils.isNotEmpty(flagUpdatedUids);
	}
	
	public Set<Long> getFlagUpdatedUids() {
		return flagUpdatedUids;
	}

	public List<Long> getCachedUids() {
		return cachedUids;
	}

	public void event(Event event) {
		if (event instanceof MessageAddedEvent) {
			reset();
			sizeChanged = true;
		} else if (event instanceof MessageExpungedEvent) {
			expunged((MessageExpungedEvent) event);
		} else if (event instanceof FlagUpdatedEvent) {
			flagsUpdated((FlagUpdatedEvent) event);
		} else if (event instanceof MailboxDeletedEvent) {
			deletedMailbox = true;
		}
	}
	
	private void expunged(MessageExpungedEvent event) {
		List<Long> uids = event.getExpungedUids();
		if (expungedUids == null) {
			expungedUids = new TreeSet<Long>(uids);
			cachedUids = DaoFactory.getMessageDao().getMessageIDList(
					event.getMailboxID());
		} else {
			expungedUids.addAll(uids);
		}
	}
	
	private void flagsUpdated(FlagUpdatedEvent event) {
		List<Long> uids = event.getFlagUpdatedUids();
		if (flagUpdatedUids == null) {
			flagUpdatedUids = new TreeSet<Long>(uids);
		} else {
			flagUpdatedUids.addAll(uids);
		}
	}
	
	private void clear(Collection<Long> uids) {
		if (uids != null) {
			uids.clear();
		}
	}
	
	public void reset() {
		sizeChanged = false;
		clear(expungedUids);
		clear(flagUpdatedUids);
		clear(cachedUids);
		expungedUids = null;
	}

}
