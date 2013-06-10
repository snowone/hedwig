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
package com.hs.mail.imap.processor.fetch;

import java.io.Serializable;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class Address implements Serializable {

	private static final long serialVersionUID = 7228265949355058768L;

	/** Empty array */
    public static final Address[] EMPTY = {};

    private final String atDomainList;

    private final String hostName;

    private final String mailboxName;

    private final String personalName;

    public Address(String atDomainList, String hostName, String mailboxName,
			String personalName) {
		super();
		this.atDomainList = atDomainList;
		this.hostName = hostName;
		this.mailboxName = mailboxName;
		this.personalName = personalName;
	}

	/**
     * Gets the personal name.
     * 
     * @return personal name, or null if the personal name is
     *         <code>NIL</code>
     */
    public String getPersonalName() {
    	return personalName;
    }

    /**
     * Gets the SMTP source route.
     * 
     * @return SMTP at-domain-list, or null if the list if
     *         <code>NIL</code>
     */
    public String getAtDomainList() {
    	return atDomainList;
    }

    /**
     * Gets the mailbox name.
     * 
     * @return the mailbox name or the group name when
     *         {@link #getHostName()} is null
     */
    public String getMailboxName() {
    	return mailboxName;
    }

    /**
     * Gets the host name.
     * 
     * @return the host name, or null when this address marks the start
     *         or end of a group
     */
    public String getHostName() {
    	return hostName;
    }
	
}
