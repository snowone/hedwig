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
package com.hs.mail.imap.dao;

import java.util.List;
import java.util.Map;

import javax.mail.Flags;

import com.hs.mail.imap.message.FetchData;
import com.hs.mail.imap.message.MailMessage;
import com.hs.mail.imap.message.MessageHeader;
import com.hs.mail.imap.message.PhysMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public interface MessageDao {

	public List<Long> getMessageIDList(long mailboxID);

	public void copyMessage(long messageID, long mailboxID);

	public FetchData getMessageFetchData(long messageID);
	
	public PhysMessage getDanglingMessageID(long messageID);

	public void addMessage(long mailboxID, MailMessage message);
	
	public void addMessage(long mailboxID, MailMessage message, Flags flags);
	
	public void deleteMessage(long messageID);
	
	public void deletePhysicalMessage(long physMessageID);
	
	public List<Long> getRevocableMessageIDList(String messageID);

	public void setFlags(long messageID, Flags flags, boolean replace, boolean set);
	
	public List<Long> resetRecent(long messageID);
	
	public Flags getFlags(long messageID);
	
	public Map<String, String> getHeader(long physMessageID);
	
	public Map<String, String> getHeader(long physMessageID, String[] fields);

	public void addHeader(long physMessageID, MessageHeader header);

	public long getHeaderNameID(String headerName);
	
}
