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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.james.mime4j.parser.Field;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.hs.mail.imap.message.FetchData;
import com.hs.mail.imap.message.MailMessage;
import com.hs.mail.imap.message.MessageHeader;
import com.hs.mail.imap.message.PhysMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 23, 2010
 *
 */
public class MySqlMessageDao extends AbstractDao implements MessageDao {

	public List<Long> getMessageIDList(long mailboxID) {
		String sql = "SELECT messageid FROM message WHERE mailboxid = ? ORDER BY messageid";
		return (List<Long>) getJdbcTemplate().queryForList(sql,
				new Object[] { new Long(mailboxID) }, Long.class);
	}

	public void addMessage(long mailboxID, MailMessage message) {
		if (message.getPhysMessageID() == 0) {
			addPhysicalMessage(message);
		}
		addMessage(message.getPhysMessageID(), mailboxID);
	}

	private long addPhysicalMessage(final MailMessage message) {
		final String sql = "INSERT INTO physmessage (size, internaldate, subject, sentdate, fromaddr) VALUES(?, ?, ?, ?, ?)";
		final MessageHeader header = message.getHeader();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement pstmt = con.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				pstmt.setLong(1, message.getSize()); // size
				pstmt.setTimestamp(2, new Timestamp(message.getInternalDate()
						.getTime())); // internaldate
				pstmt.setString(3, header.getSubject()); // subject
				Date sent = header.getDate();
				pstmt.setTimestamp(4, (sent != null) ? new Timestamp(sent
						.getTime()) : null); // sentdate
				pstmt.setString(5, (header.getFrom() != null) ? header
						.getFrom().getDisplayString() : null); // fromaddr
				return pstmt;
			}
		}, keyHolder);
		long physmessageid = keyHolder.getKey().longValue();
		message.setPhysMessageID(physmessageid);
		addHeader(physmessageid, header);
		return physmessageid;
	}

	private void addMessage(long physMessageID, long mailboxID) {
		String sql = "INSERT INTO message (physmessageid, mailboxid) VALUES(?, ?)";
		getJdbcTemplate().update(sql,
				new Object[] { new Long(physMessageID), new Long(mailboxID) });
	}

	public void addMessage(long mailboxID, MailMessage message, Flags flags) {
		if (message.getPhysMessageID() == 0) {
			addPhysicalMessage(message);
		}
		addMessage(message.getPhysMessageID(), mailboxID, flags);
	}

	private void addMessage(long physMessageID, long mailboxID, Flags flags) {
		String sql = "INSERT INTO message (physmessageid, mailboxid, seen, answered, deleted, flagged, draft) VALUES(?, ?, ?, ?, ?, ?, ?)";
		getJdbcTemplate().update(
				sql,
				new Object[] { new Long(physMessageID), new Long(mailboxID),
						FlagUtils.getParam(flags, Flags.Flag.SEEN),
						FlagUtils.getParam(flags, Flags.Flag.ANSWERED),
						FlagUtils.getParam(flags, Flags.Flag.DELETED),
						FlagUtils.getParam(flags, Flags.Flag.FLAGGED),
						FlagUtils.getParam(flags, Flags.Flag.DRAFT) });
	}
	
	public void copyMessage(long messageID, long mailboxID) {
		// The flags and internal date of the message SHOULD be preserved, and
		// the Recent flag SHOULD be set, in the copy.
		String sql = "INSERT INTO message (mailboxid, physmessageid, seen, answered, deleted, flagged, draft) SELECT ?, physmessageid, seen, answered, deleted, flagged, draft FROM message WHERE messageid = ?";
		getJdbcTemplate().update(sql,
				new Object[] { new Long(mailboxID), new Long(messageID) });
	}
	
	public FetchData getMessageFetchData(long messageID) {
		String sql = "SELECT * FROM message m, physmessage p WHERE m.messageid = ? AND m.physmessageid = p.id";
		FetchData fd = (FetchData) queryForObject(sql, new Object[] { new Long(
				messageID) }, fetchDataRowMapper);
		if (fd != null) {
			List<String> ufs = getUserFlags(messageID);
			if (CollectionUtils.isNotEmpty(ufs)) {
				Flags flags = fd.getFlags();
				for (String uf : ufs) {
					flags.add(uf);
				}
			}
		}
		return fd;
	}
	
	public void deleteMessage(long messageID) {
		String sql = "DELETE m.*, k.* FROM message AS m LEFT JOIN keyword AS k ON (m.messageid = k.messageid) WHERE m.messageid = ?";
		getJdbcTemplate().update(sql, new Object[] { new Long(messageID) });
	}

	public PhysMessage getDanglingMessageID(long messageID) {
		String sql = "SELECT m.physmessageid, p.internaldate FROM message m, physmessage p WHERE m.physmessageid = (SELECT physmessageid FROM message WHERE messageid = ?) AND p.id=m.physmessageid GROUP BY m.physmessageid HAVING COUNT(m.physmessageid) = 1";
		return (PhysMessage) queryForObject(sql,
				new Object[] { new Long(messageID) }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						PhysMessage pm = new PhysMessage();
						pm.setPhysMessageID(rs.getLong("physmessageid"));
						pm.setInternalDate(new Date(rs.getTimestamp("internaldate").getTime()));
						return pm;
					}
			}
		);
	}
	
	public void deletePhysicalMessage(long physMessageID) {
		String[] sql = { 
				"DELETE FROM physmessage WHERE id = ?",
				"DELETE FROM headervalue WHERE physmessageid = ?" 
		};
		Object[] param = { new Long(physMessageID) };
		getJdbcTemplate().update(sql[0], param);
		getJdbcTemplate().update(sql[1], param);
	}
	
	public List<Long> getRevocableMessageIDList(String messageID) {
		String sql = "SELECT messageid FROM message "
			+ "WHERE recent = 'Y' "
			+   "AND mailboxid = (SELECT mailboxid FROM mailbox WHERE name = 'INBOX') " 
			+	"AND physmessageid IN (" 
			+				"SELECT physmessageid FROM headervalue " 
			+				 "WHERE headernameid = (SELECT id FROM headername WHERE headername = 'Message-ID') " 
			+				   "AND headervalue = ?)";
		return (List<Long>) getJdbcTemplate().queryForList(sql,
				new Object[] { messageID });
	}
	
	public List<Long> resetRecent(long mailboxID) {
		String sql = "SELECT messageid FROM message WHERE mailboxid = ? AND recent = 'Y'";
		Object[] param = new Object[] { new Long(mailboxID) };
		List<Long> result = getJdbcTemplate().queryForList(sql, param,
				Long.class);
		if (CollectionUtils.isNotEmpty(result)) {
			sql = "UPDATE message SET recent = 'N' WHERE mailboxid = ? AND recent = 'Y'";
			getJdbcTemplate().update(sql, param);
		}
		return result;
	}
	
	public void setFlags(long messageID, Flags flags, boolean replace,
			boolean set) {
		setSystemFlags(messageID, flags.getSystemFlags(), replace, set);
		setUserFlags(messageID, flags.getUserFlags(), replace, set);
	}
	
	private int setSystemFlags(long messageID, Flags.Flag[] flags,
			boolean replace, boolean set) {
		if (ArrayUtils.isEmpty(flags)) {
			return 0;
		}
		StringBuilder sql = new StringBuilder("UPDATE message SET ");
		List params = new ArrayList();
		sql.append(FlagUtils.buildParams(flags, replace, set, params));
		if (params.isEmpty()) {
			return 0;
		}
		sql.append(" WHERE messageid = ?");
		params.add(new Long(messageID));
		return getJdbcTemplate().update(sql.toString(), params.toArray());
	}
	
	private void setUserFlags(long messageID, String[] flags, boolean replace,
			boolean set) {
		if (replace) {
			String sql = "DELETE FROM keyword WHERE messageid = ?";
			getJdbcTemplate().update(sql, new Object[] { new Long(messageID) });
		}
		if (!ArrayUtils.isEmpty(flags)) {
			String sql = (replace || set) ? "INSERT INTO keyword (messageid, keyword) VALUES(?, ?)"
					: "DELETE FROM keyword WHERE messageid = ? AND keyword = ?";
			for (int i = 0; i < flags.length; i++) {
				if (!(set && hasUserFlag(messageID, flags[i]))) {
					getJdbcTemplate().update(sql,
							new Object[] { new Long(messageID), flags[i] });
				}
			}
		}
	}
	
	public Flags getFlags(long messageID) {
		Flags flags = getSystemFlags(messageID);
		if (flags == null) {
			flags = new Flags();
		}
		List<String> ufs = getUserFlags(messageID);
		if (CollectionUtils.isNotEmpty(ufs)) {
			for (String uf : ufs) {
				flags.add(uf);
			}
		}
		return flags;
	}

	private Flags getSystemFlags(long messageID) {
		String sql = "SELECT * FROM message WHERE messageid = ?";
		return (Flags) queryForObject(sql,
				new Object[] { new Long(messageID) }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return FlagUtils.getFlags(rs);
					}
				});
	}
	
	private List<String> getUserFlags(long messageID) {
		String sql = "SELECT keyword FROM keyword WHERE messageid = ?";
		return getJdbcTemplate().queryForList(sql,
				new Object[] { new Long(messageID) }, String.class);
	}
	
	private boolean hasUserFlag(long messageID, String flag) {
		String sql = "SELECT COUNT(1) FROM keyword WHERE messageid = ? AND keyword = ?";
		return queryForInt(sql, new Object[] { new Long(messageID), flag }) > 0;
	}
	
//-------------------------------------------------------------------------
// Methods dealing with message header
//-------------------------------------------------------------------------

	public Map<String, String> getHeader(long physMessageID) {
		String sql = "SELECT headername, headervalue FROM headername n, headervalue v WHERE v.physmessageid = ? AND v.headernameid = n.id";
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(sql,
				new Object[] { new Long(physMessageID) });
		Map<String, String> results = new HashMap<String, String>();
		while (rs.next()) {
			results.put(rs.getString(1), rs.getString(2));
		}
		return results;
	}
	
	public Map<String, String> getHeader(long physMessageID, String[] fields) {
		String sql = "SELECT headername, headervalue FROM headername n, headervalue v WHERE v.physmessageid = ? AND v.headernameid = n.id AND n.headername IN ";
		Object[] param = new Object[fields.length + 1];
		param[0] = new Long(physMessageID);
		sql += duplicate(fields, param, 1);
		Map<String, String> results = new HashMap<String, String>();
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(sql, param);
		while (rs.next()) {
			results.put(rs.getString(1), rs.getString(2));
		}
		return results;
	}

	public void addHeader(long physMessageID, MessageHeader header) {
		List<Field> fields = header.getHeader().getFields();
		if (!CollectionUtils.isEmpty(fields)) {
			for (Field field : fields) {
				addField(physMessageID, field);
			}
		}
	}
	
	private void addField(long physMessageID, Field field) {
		long id = getHeaderNameID(field.getName());
		addHeaderValue(physMessageID, id, field.getBody());
	}

	public long getHeaderNameID(String headerName) {
		String sql = "SELECT id FROM headername WHERE headername = ?";
		long result = queryForLong(sql, new Object[] { headerName });
		if (result == 0) {
			result = addHeaderName(headerName);
		}
		return result;
	}
	
	private long addHeaderName(final String headerName) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				String sql = "INSERT INTO headername (headername) VALUES(?)";
				PreparedStatement pstmt = con.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, headerName);
				return pstmt;
			}
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}

	private void addHeaderValue(long physMessageID, long headerNameID,
			String headerValue) {
		String sql = "INSERT INTO headervalue (physmessageid, headernameid, headervalue) VALUES(?, ?, ?)";
		getJdbcTemplate().update(
				sql,
				new Object[] { new Long(physMessageID), new Long(headerNameID),
						headerValue });
	}

	 private static RowMapper fetchDataRowMapper = new RowMapper() {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			FetchData fd = new FetchData();
			fd.setMessageID(rs.getLong("messageid"));
			fd.setPhysMessageID(rs.getLong("physmessageid"));
			fd.setSize(rs.getLong("size"));
			fd.setFlags(FlagUtils.getFlags(rs));
			fd.setInternalDate(new Date(rs.getTimestamp("internaldate").getTime()));
			return fd;
		}
	 };
	 
}
