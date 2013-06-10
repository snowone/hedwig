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
package com.hs.mail.imap.user;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.mail.Quota;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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
import com.hs.mail.imap.dao.UserDao;
import com.hs.mail.imap.message.PhysMessage;
import com.hs.mail.security.login.BasicCallbackHandler;
import com.hs.mail.smtp.message.MailAddress;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 24, 2010
 *
 */
public class DefaultUserManager implements UserManager {

	private static Logger logger = Logger.getLogger(UserManager.class);
	
	private TransactionTemplate transactionTemplate;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		Assert.notNull(transactionManager, "The 'transactionManager' argument must not be null.");
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}
	
	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}
	
	/**
	 * Authenticate the given user against the given password. When
	 * authenticated, the ID of the user will be supplied.
	 * 
	 * @param username
	 *            user name
	 * @param password
	 *            password supplied
	 * @return id of the user when authenticated
	 * @throws LoginException
	 *             when the user does not exist or not authenticated
	 */
	public long login(String username, String password) throws LoginException {
		String address = toAddress(username); 
		User user = DaoFactory.getUserDao().getUserByAddress(address);
		if (user == null) {
			throw new AccountNotFoundException("Account for " + username
					+ " not found");
		}
		if (Config.getAuthScheme() != null) {
			CallbackHandler callbackHandler = new BasicCallbackHandler(address,
					password.toCharArray());
			LoginContext lc = new LoginContext(Config.getAuthScheme(),
					callbackHandler);
			lc.login();
		} else {
			if (!password.equals(user.getPassword())) {
				throw new CredentialException("Incorrect password for "
						+ username);
			}
		}
		return user.getID();
	}
	
	public User getUser(long id) {
		return DaoFactory.getUserDao().getUser(id);
	}

	public long getUserID(String address) {
		return DaoFactory.getUserDao().getUserID(address);
	}
	
	public User getUserByAddress(String address) {
		return DaoFactory.getUserDao().getUserByAddress(address);
	}

	public int getUserCount(String domain) {
		return DaoFactory.getUserDao().getUserCount(domain);
	}
	
	public List<User> getUserList(String domain, int page, int pageSize) {
		return DaoFactory.getUserDao().getUserList(domain, page, pageSize);
	}
	
	public long addUser(final User user) {
		return (Long) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							return DaoFactory.getUserDao().addUser(user);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public int updateUser(final User user) {
		return (Integer) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							return DaoFactory.getUserDao().updateUser(user);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public void deleteUser(final long id) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							UserDao dao = DaoFactory.getUserDao();
							dao.deleteUser(id);
							emptyMailboxes(id);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public void emptyUser(final long id) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							emptyMailboxes(id);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	private void emptyMailboxes(long ownerID) {
		MailboxDao dao = DaoFactory.getMailboxDao();
		List<PhysMessage> danglings = dao.getDanglingMessageIDList(ownerID);
		dao.deleteMessages(ownerID);
		dao.deleteMailboxes(ownerID);
		if (CollectionUtils.isNotEmpty(danglings)) {
			for (PhysMessage pm : danglings) {
				deletePhysicalMessage(pm);
			}
		}
	}
	
	private void deletePhysicalMessage(PhysMessage pm) {
		MessageDao dao = DaoFactory.getMessageDao();
		dao.deletePhysicalMessage(pm.getPhysMessageID());
		try {
			File file = Config.getDataFile(pm.getInternalDate(), pm.getPhysMessageID());
			FileUtils.forceDelete(file);
		} catch (IOException ex) {
			logger.warn(ex.getMessage(), ex); // Ignore - What we can do?
		}
	}
	
	public Alias getAlias(long id) {
		return DaoFactory.getUserDao().getAlias(id); 
	}
	
	public int getAliasCount(String domain) {
		return DaoFactory.getUserDao().getAliasCount(domain);
	}
	
	public List<Alias> getAliasList(String domain, int page, int pageSize) {
		return DaoFactory.getUserDao().getAliasList(domain, page, pageSize);
	}
	
	public List<Alias> expandAlias(String alias) {
		return DaoFactory.getUserDao().expandAlias(alias);
	}
	
	public long addAlias(final Alias alias) {
		return (Long) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							return DaoFactory.getUserDao().addAlias(alias);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public int updateAlias(final Alias alias) {
		return (Integer) getTransactionTemplate().execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						try {
							return DaoFactory.getUserDao().updateAlias(alias);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public void deleteAlias(final long id) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(
							TransactionStatus status) {
						try {
							UserDao dao = DaoFactory.getUserDao();
							dao.deleteAlias(id);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public long getQuotaUsage(long ownerID) {
		return DaoFactory.getUserDao().getQuotaUsage(ownerID);
	}

	public Quota getQuota(long ownerID, String quotaRoot) {
		Quota quota = DaoFactory.getUserDao().getQuota(ownerID, quotaRoot);
		if (quota.resources[0].limit == 0) {
			quota.resources[0].limit = Config.getDefaultQuota();
		}
		return quota;
	}

	public void setQuota(final long ownerID, final Quota quota) {
		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							DaoFactory.getUserDao().setQuota(ownerID, quota);
						} catch (DataAccessException ex) {
							status.setRollbackOnly();
							throw ex;
						}
					}
				});
	}
	
	public File getUserHome(MailAddress user) {
		String str = user.getUser();
		StringBuilder sb = new StringBuilder(
				(user.getHost() != null) ? user.getHost() : Config.getDefaultDomain())
				.append(File.separator)
				.append("users")
				.append(File.separator)
				.append(str.charAt(0))
				.append(str.charAt(str.length() - 1))
				.append(File.separator)
				.append(str);
		return new File(Config.getDataDirectory(), sb.toString());
	}
	
	public String getUserSieveScript(MailAddress user) {
		File script = new File(getUserHome(user), "sieve");
		if (script.exists()) {
			try {
				return FileUtils.readFileToString(script);
			} catch (IOException e) {
			}
		}
		return null;
	}
	
	private String toAddress(String user) {
		if (user.indexOf('@') != -1)
			return user;
		else
			return new StringBuffer(user)
							.append('@')
							.append(Config.getDefaultDomain())
							.toString();
	}

}
