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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.message.PhysMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 23, 2010
 *
 */
public class MySqlMailboxDao extends AbstractDao implements MailboxDao {

	public Mailbox getMailbox(long ownerID, String mailboxName) {
		String sql = "SELECT * FROM mailbox USE INDEX (fk_mailbox_user) WHERE ownerid = ? AND name = ?";
		return (Mailbox) queryForObject(sql, new Object[] { new Long(ownerID),
				mailboxName }, mailboxRowMapper);
	}

	public boolean mailboxExists(long ownerID, String mailboxName) {
		String sql = "SELECT COUNT(1) FROM mailbox USE INDEX (fk_mailbox_user) WHERE ownerid = ? AND name = ?";
		return queryForInt(sql, new Object[] { new Long(ownerID), mailboxName }) > 0;
	}

	public Mailbox createMailbox(long ownerID, String mailboxName) {
		Mailbox result = doCreateMailbox(ownerID, mailboxName);
		forceCreate(ownerID, Mailbox.getParent(mailboxName));
		return result;
	}

	public void renameMailbox(Mailbox source, String dest) {
		long ownerid = source.getOwnerID();

		// If the server¡¯s hierarchy separator character appears in the
		// name, the server SHOULD create any superior hierarchical names
		// that are needed.
		forceCreate(ownerid, Mailbox.getParent(dest));

		// If INBOX is renamed, then all messages in INBOX MUST be moved to
		// a new mailbox with the given name, leaving INBOX empty.
		String base = source.getName();
		if (ImapConstants.INBOX_NAME.equals(base)) {
			doCreateMailbox(ownerid, ImapConstants.INBOX_NAME);
		}
		doRenameMailbox(source.rename(base, dest));

		// If source name has inferior hierarchical names, then the inferior
		// hierarchical names MUST also be renamed.
		List<Mailbox> children = getChildren(ownerid, base);
		for (Mailbox child : children) {
			doRenameMailbox(child.rename(base, dest));
		}
	}

	private void forceCreate(long ownerID, String mailboxName) {
		while (!"".equals(mailboxName) && !mailboxExists(ownerID, mailboxName)) {
			doCreateMailbox(ownerID, mailboxName);
			mailboxName = Mailbox.getParent(mailboxName);
		}
	}

	private Mailbox doCreateMailbox(final long ownerID, final String mailboxName) {
		final String sql = "INSERT INTO mailbox (name, ownerid, nextuid, uidvalidity) VALUES(?, ?, ?, ?)";
		final long uidValidity = System.currentTimeMillis();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement pstmt = con.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, mailboxName);
				pstmt.setLong(2, ownerID);
				pstmt.setLong(3, 1);
				pstmt.setLong(4, uidValidity);
				return pstmt;
			}
		}, keyHolder);
		
		Mailbox mailbox = new Mailbox();
		mailbox.setMailboxID(keyHolder.getKey().longValue());
		mailbox.setOwnerID(ownerID);
		mailbox.setName(mailboxName);
		mailbox.setNextUID(1);
		mailbox.setUidValidity(uidValidity);
		
		return mailbox;
	}

	public List<Mailbox> getChildren(long userID, long ownerID,
			String mailboxName, boolean subscribed) {
		if (subscribed)
			return getSubscriptions(userID, ownerID, mailboxName);
		else
			return getChildren(ownerID, mailboxName);
	}
	
	private List<Mailbox> getChildren(long ownerID, String mailboxName) {
		if (StringUtils.isEmpty(mailboxName)) {
			String sql = "SELECT * FROM mailbox USE INDEX (fk_mailbox_user) WHERE ownerid = ? ORDER BY name";
			return getJdbcTemplate().query(sql,
					new Object[] { new Long(ownerID) }, mailboxRowMapper);
		} else {
			String sql = "SELECT * FROM mailbox USE INDEX (fk_mailbox_user) WHERE ownerid = ? AND name LIKE ? ORDER BY name";
			return getJdbcTemplate().query(
					sql,
					new Object[] {
							new Long(ownerID),
							new StringBuilder(escape(mailboxName))
									.append(Mailbox.folderSeparator).append('%')
									.toString() }, mailboxRowMapper);
		}
	}
	
	private List<Mailbox> getSubscriptions(long userID, long ownerID,
			String mailboxName) {
		if (StringUtils.isEmpty(mailboxName)) {
			String sql = "SELECT b.* FROM mailbox b, subscription s WHERE s.userid = ? AND b.ownerid = ? AND b.name = s.name ORDER BY b.name";
			return getJdbcTemplate().query(sql,
					new Object[] { new Long(userID), new Long(ownerID) },
					mailboxRowMapper);
		} else {
			String sql = "SELECT b.* FROM mailbox b, subscription s WHERE s.userid = ? AND b.ownerid = ? AND b.name LIKE ? AND b.name = s.name ORDER BY b.name";
			return getJdbcTemplate().query(
					sql,
					new Object[] {
							new Long(userID),
							new Long(ownerID),
							new StringBuilder(escape(mailboxName))
									.append(Mailbox.folderSeparator).append('%')
									.toString() }, mailboxRowMapper);
		}
	}

	public int getChildCount(long ownerID, String mailboxName) {
		if (StringUtils.isEmpty(mailboxName)) {
			String sql = "SELECT COUNT(1) FROM mailbox USE INDEX (fk_mailbox_user) WHERE ownerid = ?";
			Object[] params = { new Long(ownerID) };
			return queryForInt(sql, params);
		} else {
			String sql = "SELECT COUNT(1) FROM mailbox USE INDEX (fk_mailbox_user) WHERE ownerid = ? AND name LIKE ?";
			Object[] params = {
					new Long(ownerID),
					new StringBuilder(escape(mailboxName)).append(
							Mailbox.folderSeparator).append('%').toString() };
			return queryForInt(sql, params);
		}
	}
	
	public List<Long> getMailboxIDList(String mailboxName) {
		if (mailboxName.endsWith("*")) {
			String sql = "SELECT mailboxid FROM mailbox WHERE name LIKE ?";
			return (List<Long>) getJdbcTemplate().queryForList(
					sql,
					new Object[] { new StringBuilder(escape(mailboxName))
							.append('%').toString() }, Long.class);
		} else {
			String sql = "SELECT mailboxid FROM mailbox WHERE name = ?";
			return (List<Long>) getJdbcTemplate().queryForList(sql,
					new Object[] { mailboxName }, Long.class);
		}
	}

	public boolean isSubscribed(long userID, String mailboxName) {
		String sql = "SELECT COUNT(1) FROM subscription WHERE userid = ? AND name = ?";
		int count = getJdbcTemplate().queryForInt(sql,
				new Object[] { new Long(userID), mailboxName });
		return (count > 0);
	}

	public void addSubscription(long userID, long mailboxID, String mailboxName) {
		if (!isSubscribed(userID, mailboxName)) {
			String sql = "INSERT INTO subscription (userid, mailboxid, name) VALUES(?, ?, ?)";
			getJdbcTemplate().update(
					sql,
					new Object[] { new Long(userID), new Long(mailboxID),
							mailboxName });
		} else {
			// already subscribed to the mailbox, verified after attempt to
			// subscribe
		}
	}

	public void deleteSubscription(long userID, String mailboxName) {
		String sql = "DELETE FROM subscription WHERE userid = ? AND name = ?";
		getJdbcTemplate().update(sql,
				new Object[] { new Long(userID), mailboxName });
	}

	private int doRenameMailbox(Mailbox mailbox) {
		String sql = "UPDATE mailbox SET name = ? WHERE mailboxid = ?";
		return getJdbcTemplate().update(
				sql,
				new Object[] { mailbox.getName(),
						new Long(mailbox.getMailboxID()) });
	}

	public List<Long> getDeletedMessageIDList(long mailboxID) {
		String sql = "SELECT messageid FROM message WHERE mailboxid = ? AND deleted = 'Y'";
		return (List<Long>) getJdbcTemplate().queryForList(sql,
				new Object[] { new Long(mailboxID) }, Long.class);
	}

	public void deleteMailboxes(long ownerID) {
		String sql = "DELETE FROM mailbox WHERE ownerid = ?";
		getJdbcTemplate().update(sql, new Object[] { new Long(ownerID) });
	}
	
	public void deleteMailbox(long ownerID, long mailboxID) {
		String sql = "DELETE FROM mailbox WHERE mailboxid = ?";
		getJdbcTemplate().update(sql, new Object[] { new Long(mailboxID) });
	}

	public void forbidSelectMailbox(long ownerID, long mailboxID) {
		String sql = "UPDATE mailbox SET noselect = 'Y' WHERE mailboxid = ?";
		getJdbcTemplate().update(sql, new Object[] { new Long(mailboxID) });
	}

	public List<PhysMessage> getDanglingMessageIDList(long ownerID) {
		String sql = "SELECT m.physmessageid, p.internaldate FROM mailbox b, message m, physmessage p WHERE b.ownerid = ? AND m.physmessageid = p.id AND m.mailboxid = b.mailboxid GROUP BY m.physmessageid HAVING COUNT(m.physmessageid) = 1";
		return (List<PhysMessage>) getJdbcTemplate().query(sql,
				new Object[] { new Long(ownerID) }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						PhysMessage pm = new PhysMessage();
						pm.setPhysMessageID(rs.getLong("physmessageid"));
						pm.setInternalDate(new Date(rs.getTimestamp("internaldate").getTime()));
						return pm;
					}

				});
	}
	
	public List<PhysMessage> getDanglingMessageIDList(long ownerID,
			long mailboxID) {
		String sql = "SELECT m.physmessageid, p.internaldate FROM message m, message n, physmessage p WHERE m.mailboxid = ? AND m.physmessageid = n.physmessageid AND m.physmessageid = p.id GROUP BY n.physmessageid HAVING COUNT(n.physmessageid) = 1";
		return (List<PhysMessage>) getJdbcTemplate().query(sql,
				new Object[] { new Long(mailboxID) }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						PhysMessage pm = new PhysMessage();
						pm.setPhysMessageID(rs.getLong("physmessageid"));
						pm.setInternalDate(new Date(rs.getTimestamp("internaldate").getTime()));
						return pm;
					}

				});
	}

	public void deleteMessages(long ownerID) {
		String sql = "DELETE m.*, k.* FROM message AS m LEFT JOIN keyword AS k ON (m.messageid = k.messageid) WHERE EXISTS (SELECT 1 FROM mailbox WHERE ownerid = ?)";
		getJdbcTemplate().update(sql, new Object[] { new Long(ownerID) });
	}

	public void deleteMessages(long ownerID, long mailboxID) {
		String sql = "DELETE m.*, k.* FROM message AS m LEFT JOIN keyword AS k ON (m.messageid = k.messageid) WHERE m.mailboxid = ?";
		getJdbcTemplate().update(sql, new Object[] { new Long(mailboxID) });
	}

	public int getMessageCount(long mailboxID) {
		String sql = "SELECT COUNT(1) FROM message WHERE mailboxid = ?";
		return queryForInt(sql, new Object[] { new Long(mailboxID) });
	}
	
	public int getRecentMessageCount(long mailboxID) {
		String sql = "SELECT COUNT(1) FROM message WHERE mailboxid = ? AND recent = 'Y'";
		return queryForInt(sql, new Object[] { new Long(mailboxID) });
	}
	
	public int getUnseenMessageCount(long mailboxID) {
		String sql = "SELECT COUNT(1) FROM message WHERE mailboxid = ? AND seen = 'N'";
		return getJdbcTemplate().queryForInt(sql,
				new Object[] { new Long(mailboxID) });
	}
	
	public long getFirstUnseenMessageID(long mailboxID) {
		String sql = "SELECT messageid FROM message WHERE mailboxid = ? AND seen = 'N' ORDER BY messageid LIMIT 1";
		return queryForLong(sql, new Object[] { new Long(mailboxID) });
	}
	
	public List<Long> getGarbageMailboxList() {
		String sql = "SELECT m.mailboxid FROM mailbox AS m LEFT JOIN user AS u ON m.ownerid = u.id WHERE u.id IS NULL";
		return getJdbcTemplate().queryForList(sql, Long.class);
	}
	
	 private static RowMapper mailboxRowMapper = new RowMapper() {
		 public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Mailbox mailbox = new Mailbox();
			mailbox.setMailboxID(rs.getLong("mailboxid"));
			mailbox.setOwnerID(rs.getLong("ownerid"));
			mailbox.setName(rs.getString("name"));
			mailbox.setNoInferiors("Y".equals(rs.getString("noinferiors")));
			mailbox.setNoSelect("Y".equals(rs.getString("noselect")));
			mailbox.setReadOnly("Y".equals(rs.getString("readonly")));
			mailbox.setNextUID(rs.getLong("nextuid"));
			mailbox.setUidValidity(rs.getLong("uidvalidity"));
			return mailbox;
		}
	 };
 
}
