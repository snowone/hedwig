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
package com.hs.mail.imap.mailbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Flags;

import net.sf.ehcache.Ehcache;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.dao.DaoFactory;
import com.hs.mail.imap.dao.MailboxDao;
import com.hs.mail.imap.dao.MessageDao;
import com.hs.mail.imap.dao.SearchDao;
import com.hs.mail.imap.event.EventDispatcher;
import com.hs.mail.imap.event.EventListener;
import com.hs.mail.imap.message.FetchData;
import com.hs.mail.imap.message.MailMessage;
import com.hs.mail.imap.message.PhysMessage;
import com.hs.mail.imap.message.search.AllKey;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SortKey;
import com.hs.mail.util.EhCacheWrapper;

/**
 * 
 * @author WonChul Doh
 * @since Feb 2, 2010
 * 
 */
public class DefaultMailboxManager implements MailboxManager, DisposableBean {

	private static Logger logger = Logger.getLogger(DefaultMailboxManager.class);
	
	private TransactionTemplate transactionTemplate;
	private EhCacheWrapper<Long, FetchData> fdCache;
	private EhCacheWrapper<Long, Map<String, String>> hdCache;
	private EventDispatcher eventDispatcher = new EventDispatcher();

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		Assert.notNull(transactionManager,
				"The 'transactionManager' argument must not be null.");
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setFetchDataCache(Ehcache cache) {
		this.fdCache = new EhCacheWrapper<Long, FetchData>(cache);
	}

	public void setHeaderCache(Ehcache cache) {
		this.hdCache = new EhCacheWrapper<Long, Map<String, String>>(cache);
	}
	
	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public void addEventListener(EventListener listener) {
		if (listener != null)
			eventDispatcher.addEventListener(listener);
	}

	public void removeEventListener(EventListener listener) {
		if (listener != null)
			eventDispatcher.removeEventListener(listener);
	}

	public Mailbox getMailbox(long ownerID, String mailboxName) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.getMailbox(ownerID, mailboxName);
	}

	public boolean mailboxExists(long ownerID, String mailboxName) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.mailboxExists(ownerID, mailboxName);
	}

	public List<Mailbox> getChildren(long userID, long ownerID,
			String mailboxName, boolean subscribed) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.getChildren(userID, ownerID, mailboxName, subscribed);
	}
	
	public List<Long> getMailboxIDList(String mailboxName) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.getMailboxIDList(mailboxName);
	}

	public boolean hasChildren(Mailbox mailbox) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.getChildCount(mailbox.getOwnerID(), mailbox.getName()) > 0;
	}

	public List<Long> expunge(long mailboxID) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.getDeletedMessageIDList(mailboxID);
	}

	public List<Long> search(UidToMsnMapper map, long mailboxID, SearchKey key,
			List<SortKey> sortKeys) {
		SearchDao dao = DaoFactory.getSearchDao();
		if (sortKeys == null) {
			return dao.query(map, mailboxID, key);
		} else if (key instanceof AllKey) {
			return dao.sort(mailboxID, sortKeys.get(0));
		} else {
			List<Long> searched = dao.query(map, mailboxID, key);
			List<Long> result = new ArrayList<Long>(searched.size());
			List<Long> sorted = dao.sort(mailboxID, sortKeys.get(0));
			Iterator<Long> iterator = sorted.iterator();
			while (iterator.hasNext()) {
				Long v = iterator.next();
				if (searched.contains(v)) {
					result.add(v);
				}
			}
			return result;
		}
	}

	public Mailbox createMailbox(final long ownerID, final String mailboxName) {
		return (Mailbox) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							MailboxDao dao = DaoFactory.getMailboxDao();
							return dao.createMailbox(ownerID, mailboxName);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public void renameMailbox(final Mailbox source, final String targetName) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MailboxDao dao = DaoFactory.getMailboxDao();
							dao.renameMailbox(source, targetName);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public void deleteMailbox(final long ownerID, final long mailboxID,
			final boolean delete) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MailboxDao dao = DaoFactory.getMailboxDao();
							List<PhysMessage> danglings = dao
									.getDanglingMessageIDList(ownerID,
											mailboxID);
							// We must evict objects for these deleted messages.
							// But, how we can do it?
							dao.deleteMessages(ownerID, mailboxID);
							if (delete) {
								dao.deleteMailbox(ownerID, mailboxID);
							} else {
								// prevent the mailbox from selecting
								dao.forbidSelectMailbox(ownerID, mailboxID);
							}
							if (CollectionUtils.isNotEmpty(danglings)) {
								for (PhysMessage pm : danglings) {
									if (hdCache != null) {
										hdCache.remove(pm.getPhysMessageID());
									}
									deletePhysicalMessage(pm);
								}
							}
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	private void deletePhysicalMessage(PhysMessage pm) {
		MessageDao dao = DaoFactory.getMessageDao();
		dao.deletePhysicalMessage(pm.getPhysMessageID());
		if (hdCache != null) {
			hdCache.remove(pm.getPhysMessageID());
		}
		try {
			File dataFile = Config.getDataFile(pm.getInternalDate(),
					pm.getPhysMessageID());
			FileUtils.forceDelete(dataFile);
			File descriptorFile = Config.getMimeDescriptorFile(
					pm.getInternalDate(), pm.getPhysMessageID());
			if (descriptorFile.exists()) {
				FileUtils.forceDelete(descriptorFile);
			}
		} catch (IOException ex) {
			logger.warn(ex.getMessage(), ex); // Ignore - What we can do?
		}
	}

	public boolean isSubscribed(long userID, String mailboxName) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		return dao.isSubscribed(userID, mailboxName);
	}

	public void addSubscription(final long userID, final long mailboxID,
			final String mailboxName) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MailboxDao dao = DaoFactory.getMailboxDao();
							dao.addSubscription(userID, mailboxID, mailboxName);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public void deleteSubscription(final long userID, final String mailboxName) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MailboxDao dao = DaoFactory.getMailboxDao();
							dao.deleteSubscription(userID, mailboxName);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public FetchData getMessageFetchData(long uid) {
		FetchData fd = (fdCache != null) ? fdCache.get(uid) : null;
		if (fd == null) {
			MessageDao dao = DaoFactory.getMessageDao();
			fd = dao.getMessageFetchData(uid);
			if (fdCache != null && fd != null) {
				fdCache.put(uid, fd);
			} else if (fd == null) {
				logger.error("Failed to retrieve fetch data for message ["
						+ uid + "]");
			}
		}
		return fd;
	}

	public Flags getFlags(long uid) {
		MessageDao dao = DaoFactory.getMessageDao();
		return dao.getFlags(uid);
	}

	public List<Long> getMessageIDList(long mailboxID) {
		MessageDao dao = DaoFactory.getMessageDao();
		return dao.getMessageIDList(mailboxID);
	}

	public void addMessage(final long ownerID, final MailMessage message,
			String mailboxName) {
		Mailbox mailbox = getMailbox(ownerID, mailboxName);
		if (mailbox == null) {
			mailbox = createMailbox(ownerID, mailboxName);
		}
		final long mailboxID = mailbox.getMailboxID();
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MessageDao dao = DaoFactory.getMessageDao();
							dao.addMessage(mailboxID, message);
							eventDispatcher.added(mailboxID);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public MailMessage appendMessage(long mailboxID, Date internalDate, Flags flags,
			File file) throws IOException {
		// If a date-time is specified, the internal date SHOULD be set in
		// the resulting message; otherwise, the internal date of the
		// resulting message is set to the current date and time by default.
		if (internalDate == null) {
			internalDate = new Date();
		}

		// If a flag parenthesized list is specified, the flags SHOULD be
		// set in the resulting message; otherwise, the flag list of the
		// resulting message is set to empty by default.
		if (flags == null) {
			flags = new Flags();
		}

		MailMessage message = MailMessage.createMailMessage(file, internalDate,
				flags);
		appendMessage(mailboxID, message);

		// Save the message file
		message.save(true);
		
		return message;
	}

	private long appendMessage(final long mailboxID, final MailMessage message) {
		return (Long) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							MessageDao dao = DaoFactory.getMessageDao();
							dao.addMessage(mailboxID, message, message.getFlags());
							eventDispatcher.added(mailboxID);
							return message.getPhysMessageID();
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public void deleteMessage(final long uid) {
		if (fdCache != null) {
			fdCache.remove(uid);
		}
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MessageDao dao = DaoFactory.getMessageDao();
							PhysMessage pm = dao.getDanglingMessageID(uid);
							dao.deleteMessage(uid);
							if (pm != null) {
								deletePhysicalMessage(pm);
							}
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public List<Long> getRevocableMessageIDList(String messageID) {
		MessageDao dao = DaoFactory.getMessageDao();
		return dao.getRevocableMessageIDList(messageID);
	}
	
	public void copyMessage(final long uid, final long mailboxID) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MessageDao dao = DaoFactory.getMessageDao();
							dao.copyMessage(uid, mailboxID);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	public void resetRecent(final long mailboxID) {
		List<Long> recents = (List<Long>) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							MessageDao dao = DaoFactory.getMessageDao();
							return dao.resetRecent(mailboxID);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
		if (fdCache != null && CollectionUtils.isNotEmpty(recents)) {
			for (long uid : recents) {
				fdCache.remove(uid);
			}
		}
	}

	public void setFlags(final long uid, final Flags flags,
			final boolean replace, final boolean set) {
		if (fdCache != null) {
			fdCache.remove(uid);
		}
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							MessageDao dao = DaoFactory.getMessageDao();
							dao.setFlags(uid, flags, replace, set);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}

	/**
	 * Gets the headers of the message.
	 * 
	 * @param physMessageID
	 *            ID of the physical message
	 * @return map containing header name and value entries
	 */
	public Map<String, String> getHeader(long physMessageID) {
		MessageDao dao = DaoFactory.getMessageDao();
		Map<String, String> results = dao.getHeader(physMessageID);
		if (hdCache != null && hdCache.get(physMessageID) == null) {
			hdCache.put(physMessageID, defaultHeader(results));
		}
		return results;
	}
	
	private Map<String, String> defaultHeader(Map<String, String> results) {
		Map<String, String> header = new HashMap<String, String>();
		if (results != null) {
			for (String field : Config.getDefaultCacheFields()) {
				header.put(field, results.get(field));
			}
		}
		return header;
	}
	
	public Map<String, String> getHeader(long physMessageID, String[] fields) {
		Map<String, String> header = (hdCache != null) ? hdCache
				.get(physMessageID) : null;
		if (header != null) {
			return getHeaderFromCache(header, physMessageID, fields);
		} else {
			return getHeaderFromStorage(physMessageID, fields);
		}
	}
	
	private String[] getCacheFields(String[] fields) {
		Set<String> defaultCacheFields = Config.getDefaultCacheFields();
		for (int i = 0; i < fields.length; i++) {
			if (!containsIgnoreCase(defaultCacheFields, fields[i])) {
				defaultCacheFields.add(fields[i]);
			}
		}
		return defaultCacheFields
				.toArray(new String[defaultCacheFields.size()]);
	}
	
	private Map<String, String> getHeaderFromStorage(long physMessageID,
			String[] fields) {
		MessageDao dao = DaoFactory.getMessageDao();
		String[] cacheFields = getCacheFields(fields);
		Map<String, String> header = dao.getHeader(physMessageID, cacheFields);
		if (header != null) {
			if (header.size() < fields.length) {
				for (int i = 0; i < fields.length; i++) {
					if (!containsIgnoreCase(header.keySet(), fields[i])) {
						header.put(fields[i], null);
					}
				}
			}
			if (hdCache != null) {
				hdCache.put(physMessageID, header);
			}
		}
		return header;
	}
	
	private Map<String, String> getHeaderFromCache(Map<String, String> header,
			long physMessageID, String[] fields) {
		String[] uncachedFields = getUncachedFields(header, fields);
		if (uncachedFields != null) {
			MessageDao dao = DaoFactory.getMessageDao();
			Map<String, String> uncachedHeader = dao.getHeader(physMessageID,
					uncachedFields);
			if (uncachedHeader != null) {
				for (int i = 0; i < uncachedFields.length; i++) {
					header.put(uncachedFields[i],
							uncachedHeader.get(uncachedFields[i]));
				}
			} else {
				for (int i = 0; i < uncachedFields.length; i++) {
					header.put(uncachedFields[i], null);
				}
			}
		}
		return header;
	}
	
	private String[] getUncachedFields(Map<String, String> header, String[] fields) {
		List<String> uncachedFields = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			if (!containsIgnoreCase(header.keySet(), fields[i])) {
				uncachedFields.add(fields[i]);
			}
		}
		if (uncachedFields.size() > 0)
			return uncachedFields.toArray(new String[uncachedFields.size()]);
		else
			return null;
	}

	public void destroy() throws Exception {
		EhCacheWrapper.flush(fdCache);
		EhCacheWrapper.flush(hdCache);
	}
	
	private static boolean containsIgnoreCase(Collection<String> fields,
			String name) {
		for (String field : fields) {
			if (field.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
}
