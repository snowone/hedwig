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
package com.hs.mail.web.fetchmail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.log4j.Logger;

import com.hs.mail.util.ObjectLocker;
import com.hs.mail.util.ObjectLockerFactory;
import com.hs.mail.web.util.MailUtils;
import com.sun.mail.pop3.POP3Folder;

/**
 * Gateway between an external message store and Hedwig. Mail is fetched from
 * the external message store and injected into the Hedwig message store.
 * 
 * @author Won Chul Doh
 * @since Sep 2, 2010
 * 
 */
public class FetchMailer {
	
	private static Logger log = Logger.getLogger(FetchMailer.class);
	
	private final static String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	private FetchAccount account;
	
	private Folder dest;

	private Store store;

	private Folder mbox;
	
	private int fetchSize = 50;
	
	public FetchMailer(FetchAccount account, Folder dest) {
		this.account = account;
		this.dest = dest;
	}
	
	public boolean connect() throws MessagingException {
		Properties props = System.getProperties();
		if (account.isUseSSL()) {
			props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
			props.setProperty("mail.pop3.port", "995");
			props.setProperty("mail.pop3.socketFactory.port", "995");
		}
		Session session = Session.getDefaultInstance(props, null);
		store = session.getStore("pop3");
		store.connect(account.getServerName(), account.getUserName(), account
				.getPassword());
		if (log.isDebugEnabled()) {
			log.debug("Connected to " + account.getServerName());
		}
		mbox = store.getDefaultFolder();
		mbox = mbox.getFolder("INBOX");
		mbox.open(Folder.READ_WRITE);
		return true;
	}
	
	public boolean disconnect(boolean expunge, int[] msgnums) {
		try {
			close(expunge, msgnums);
			return true;
		} catch (MessagingException e) {
			return false;
		} finally {
			if (store != null && store.isConnected()) {
				try {
					store.close();
				} catch (MessagingException e) {
				}
			}
		}
	}
	
	public int fetch() throws MessagingException {
		ObjectLocker locker = ObjectLockerFactory.getInstance();
		try {
			locker.lock(account);
		} catch (Exception e) {
			// lock failed
			log.error(e.getMessage(), e);
			return 0;
		}
		boolean expunge = account.isAutoEmpty();
		int fetched = 0;
		try {
			connect();
			if (existNewMessages()) {
				int begin = 1;
				int count = getTotalMessageCount();
				if (count > begin) {
					int end = begin + fetchSize - 1;
					// Paginate to minimize the heap usage 
					while (end <= count) {
						// Get messages from begin to end
						fetched +=  appendMessages(begin, end, expunge);
						begin += fetchSize;
						end += fetchSize;
					}
					// Get the remaining messages
					fetched += appendMessages(begin, count, expunge);
				}
			}
		} catch (MessagingException e) {
			throw e;
		} finally {
			disconnect(expunge, null);
			locker.unlock(account);
		}
		return fetched;
	}
	
	private int appendMessages(int begin, int end, boolean expunge)
			throws MessagingException {
		Message[] msgs = null;
		if (begin < end) {
			// Get messages from begin to end
			if (log.isDebugEnabled()) {
				log.debug("Fetch messages from " + begin + " to " + end);
			}
			msgs = retrieveMessages(begin, end);
			if (msgs != null && msgs.length > 0) {
				int[] msgnums = new int[msgs.length];
				try {
					dest.open(Folder.READ_WRITE);
					for (int i = 0; i < msgs.length; i++) {
						// Save the original message numbers
						msgnums[i] = msgs[i].getMessageNumber();
					}
					// TODO If exception is thrown here, we must recalculate the
					// XUID and only appended message must be deleted from POP3
					// server.
					dest.appendMessages(msgs);
					dest.close(false);
					if (expunge) {
						delete(expunge, msgnums);
					} else {
						// Update last XUID and received date
						// TODO Save account
					}
				} catch (MessagingException e) {
					// Rethrow exception
					throw e;
				} finally {
					if (dest != null && dest.isOpen()) {
						try {
							dest.close(false);
						} catch (MessagingException mesx) {
							// don't care, the specs say it IS closed anyway
						}
					}
				}
			}
		}
		return (msgs != null) ? msgs.length : 0;
	}
	
	private boolean delete(boolean expunge, int[] msgnums) {
		try {
			if (mbox != null && mbox.isOpen()) {
				if (expunge && msgnums != null && msgnums.length > 0) {
					mbox.setFlags(msgnums, new Flags(Flags.Flag.DELETED), true);
				}
			}
			return true;
		} catch (MessagingException mex) {
			return false;
		}
	}
	
	private Message[] retrieveMessages(int begin, int end) {
		if (begin < end) {
			try {
				Message[] msgs = getMbox().getMessages(begin, end);
				if (!account.isAutoEmpty()
						&& account.getLastReceivedDate() != null) {
					msgs = getNewMessages(msgs);
				}
				if (msgs != null && msgs.length > 0) {
					try {
						POP3Folder pf = (POP3Folder) mbox;
						String XUID = pf.getUID(msgs[msgs.length - 1]);
						Date date = MailUtils.getReceivedDate(msgs[msgs.length - 1]);
						account.setLastXUID(XUID);
						account.setLastReceivedDate(date);
					} catch (MessagingException e) {
						// Ignore this error
						log.warn(e.getMessage());
					}
				}
				return msgs;
			} catch (MessagingException e) {
				log.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	private Message[] getNewMessages(Message[] msgs) {
		String XUID = account.getLastXUID();
		Date date = account.getLastReceivedDate();
		POP3Folder pf = (POP3Folder) mbox;
		List<Message> results = new ArrayList<Message>();
		boolean done = false;
		// The received date of incoming mail does not guarantee the order of
		// incoming mails.
		// So, we check the XUID first.
		for (int i = msgs.length - 1; i >= 0; i--) {
			try {
				String tempXUID = pf.getUID(msgs[i]);
				if (tempXUID != null) {
					if (tempXUID.equalsIgnoreCase(XUID)) {
						for (int j = i + 1; j < msgs.length; j++) {
							results.add(msgs[j]);
						}
						done = true;
						break;
					}
				} else {
					break;
				}
			} catch (MessagingException e) {
				log.warn(e.getMessage());
				break;
			}
		}
		if (!done) {
			for (int i = 0; i < msgs.length; i++) {
				if (date.before(MailUtils.getReceivedDate(msgs[i]))) {
					results.add(msgs[i]);
				}
			}
		}
		return (results.size() > 0) 
				? results.toArray(new Message[results.size()]) 
				: null;
	}
	
	private Folder getMbox() throws MessagingException {
		boolean loggedIn = true;
		if (null == mbox || !mbox.isOpen()) {
			loggedIn = false;
			for (int i = 0; i < 2; i++) {
				try {
					connect();
					if (mbox.isOpen()) {
						loggedIn = true;
						break;
					}
					Thread.sleep(1000);
				} catch (Exception e) {
					loggedIn = false;
				}
			}
		}
		if (!loggedIn) {
			throw new MessagingException("Can not open mailbox");
		}
		return mbox;
	}
	
	private int getTotalMessageCount() {
		try {
			return getMbox().getMessageCount();
		} catch (MessagingException e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	private boolean existNewMessages() {
		int messageCount = getTotalMessageCount();
		if (log.isDebugEnabled()) {
			log.debug("Total " + messageCount + " messages exist.");
		}
		if (messageCount > 0) {
			if (!account.isAutoEmpty()) {
				String XUID = account.getLastXUID();
				POP3Folder pf = (POP3Folder) mbox;
				try {
					Message msg = getMbox().getMessage(messageCount);
					String tempXUID = pf.getUID(msg);
					if (tempXUID != null) {
						if (tempXUID.equalsIgnoreCase(XUID)) {
							return false;
						}
					}
				} catch (MessagingException e) {
					log.error(e.getMessage(), e);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void close(boolean expunge, int[] msgnums)
			throws MessagingException {
		if (mbox != null && mbox.isOpen()) {
			if (expunge && msgnums != null && msgnums.length > 0) {
				mbox.setFlags(msgnums, new Flags(Flags.Flag.DELETED), true);
			}
			mbox.close(expunge);
		}
	}

}
