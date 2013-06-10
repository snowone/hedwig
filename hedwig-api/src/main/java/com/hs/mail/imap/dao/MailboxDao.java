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

import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.message.PhysMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public interface MailboxDao {

	/**
	 * Get the named mailbox.
	 * 
	 * @param ownerID
	 *            the id of the user who owns the mailbox
	 * @param mailboxName
	 *            fully qualified name of the mailbox
	 * @return Mailbox object
	 */
	public Mailbox getMailbox(long ownerID, String mailboxName);

	/**
	 * Get all child mailboxes of named mailbox.
	 * 
	 * @param userID
	 *            ID of user who called this method
	 * @param ownerID
	 *            ID of user who owns the mailbox
	 * @param mailboxName
	 *            fully qualified name of the parent mailbox
	 * @param subscribed
	 *            If true, only subscribed mailboxes are returned
	 * @return list of child Mailbox objects
	 */
	public List<Mailbox> getChildren(long userID, long ownerID,
			String mailboxName, boolean subscribed);

	/**
	 * Get the child mailboxes count.
	 * 
	 * @param ownerID
	 *            ID of the user who owns the mailbox
	 * @param mailboxName
	 *            fully qualified name of the parent mailbox
	 * @return total number of child mailboxes
	 */
	public int getChildCount(long ownerID, String mailboxName);

	/**
	 * Get the identifiers of mailboxes whose name match the given name.
	 * 
	 * @param mailboxName
	 *            fully qualified name of the mailboxes to find
	 * @return list of mailbox identifiers
	 */
	public List<Long> getMailboxIDList(String mailboxName);
	
	/**
	 * Get the deleted message's identifiers.
	 * 
	 * @param mailboxID
	 *            id of the mailbox
	 * @return list of message identifies which has \Delete message flag
	 */
	public List<Long> getDeletedMessageIDList(long mailboxID);

	/**
	 * Get the list of physical message identifiers which will be dangling
	 * pointers (pointing messages which are already deleted) after deleting all
	 * the messages owned by the user.
	 * 
	 * @param ownerID
	 *            ID of the user
	 * @return list of PhysMessage objects
	 */
	public List<PhysMessage> getDanglingMessageIDList(long ownerID);
	
	/**
	 * Get the list of physical message identifiers which will be dangling
	 * pointers (pointing messages which are already deleted) after expunging
	 * the mailbox.
	 * 
	 * @param ownerID
	 *            ID of the user who owns the mailbox
	 * @param mailboxID
	 *            ID of the mailbox
	 * @return list of PhysMessage objects
	 */
	public List<PhysMessage> getDanglingMessageIDList(long ownerID, long mailboxID);
	
	/**
	 * Check whether the mailbox really exists.
	 * 
	 * @param ownerID
	 *            ID of the user who owns the mailbox
	 * @param mailboxName
	 *            fully qualified name of the parent mailbox
	 * @return true if the mailbox exists, otherwise false
	 */
	public boolean mailboxExists(long ownerID, String mailboxName);
	
	/**
	 * Create a mailbox with given name.
	 * 
	 * @param ownerID
	 *            ID of the user who owns the mailbox
	 * @param mailboxName
	 *            fully qualified name of the mailbox to create
	 * @return Mailbox object created
	 */
	public Mailbox createMailbox(long ownerID, String mailboxName);
	
	/**
	 * Rename the mailbox.
	 * 
	 * @param source
	 *            the mailbox to rename
	 * @param dest
	 *            new name for the mailbox
	 */
	public void renameMailbox(Mailbox source, String dest);
	
	/**
	 * Delete all the mailboxes owned by the user.
	 * 
	 * @param ownerID
	 *            ID of the user
	 */
	public void deleteMailboxes(long ownerID);
	/**
	 * Delete the mailbox.
	 * 
	 * @param ownerID
	 *            ID of the user
	 * @param mailboxID
	 *            ID of the mailbox
	 */
	public void deleteMailbox(long ownerID, long mailboxID);

	/**
	 * Delete all the messages owned by the user.
	 * 
	 * @param ownerID
	 *            ID of the user
	 */
	public void deleteMessages(long ownerID);
	
	/**
	 * Delete all the messages in the mailbox.
	 * 
	 * @param ownerID
	 *            ID of the user
	 * @param mailboxID
	 *            ID of the mailbox
	 */
	public void deleteMessages(long ownerID, long mailboxID);

	/**
	 * Set \Noselect mailbox name attribute.
	 * 
	 * @param ownerID
	 *            ID of the user
	 * @param mailboxID
	 *            ID of the mailbox
	 */
	public void forbidSelectMailbox(long ownerID, long mailboxID);
	
	/**
	 * Check whether the mailbox is subscribed.
	 * 
	 * @param userID
	 *            ID of the user
	 * @param mailboxName
	 *            fully qualified name of the mailbox
	 * @return true if the mailbox is subscribed, otherwise false
	 */
	public boolean isSubscribed(long userID, String mailboxName);
	
	/**
	 * Subscribe the mailbox.
	 * 
	 * @param userID
	 *            ID of the user
	 * @param mailboxID
	 *            ID of the mailbox to subscribe
	 * @param mailboxName
	 *            fully qualified name of the mailbox
	 */
	public void addSubscription(long userID, long mailboxID, String mailboxName);

	/**
	 * Unsubscribe the mailbox.
	 * 
	 * @param userID
	 *            ID of the user
	 * @param mailboxName
	 *            fully qualified name of the mailbox to unsubscribe
	 */
	public void deleteSubscription(long userID, String mailboxName);
	
	/**
	 * Get the total message count.
	 * 
	 * @param mailboxID
	 *            ID of the mailbox
	 * @return total number of messages
	 */
	public int getMessageCount(long mailboxID);

	/**
	 * Get the recent message count.
	 * 
	 * @param mailboxID
	 *            ID of the mailbox
	 * @return number of recent messages
	 */
	public int getRecentMessageCount(long mailboxID);

	/**
	 * Get the unseen message count.
	 * 
	 * @param mailboxID
	 *            ID of the mailbox
	 * @return total number of unseen messages
	 */
	public int getUnseenMessageCount(long mailboxID);

	/**
	 * Get the message identifier of the first unseen message.
	 * 
	 * @param mailboxID
	 *            ID of the mailbox.
	 * @return ID of the first unseen message
	 */
	public long getFirstUnseenMessageID(long mailboxID);

}
