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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.search.AllKey;
import com.hs.mail.imap.message.search.AndKey;
import com.hs.mail.imap.message.search.CompositeKey;
import com.hs.mail.imap.message.search.HeaderKey;
import com.hs.mail.imap.message.search.KeywordKey;
import com.hs.mail.imap.message.search.NotKey;
import com.hs.mail.imap.message.search.OrKey;
import com.hs.mail.imap.message.search.RecipientStringKey;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SearchKeyList;
import com.hs.mail.imap.message.search.SequenceKey;
import com.hs.mail.imap.message.search.SortKey;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 5, 2010
 *
 */

@SuppressWarnings("unchecked")
public class MySqlSearchDao extends AbstractDao implements SearchDao {

	public List<Long> query(UidToMsnMapper map, long mailboxID, SearchKey key) {
		if (key instanceof AndKey) {
			return query(map, mailboxID, (AndKey) key);
		} else if (key instanceof OrKey) {
			return query(map, mailboxID, (OrKey) key);
		} else if (key instanceof NotKey) {
			return query(map, mailboxID, (NotKey) key);
		} else if (key instanceof AllKey) {
			return map.getUIDList();
		} else if (key instanceof HeaderKey) {
			return query(map, mailboxID, (HeaderKey) key);
		} else if (key instanceof RecipientStringKey) {
			return query(map, mailboxID, (RecipientStringKey) key);
		} else if (key instanceof SequenceKey) {
			return query(map, mailboxID, (SequenceKey) key);
		} else if (key instanceof KeywordKey) {
			return query(map, mailboxID, (KeywordKey) key);
		} else if (key instanceof CompositeKey) {
			return query(map, mailboxID, (CompositeKey) key);
		} else {
			return query(map, mailboxID, new CompositeKey(key));
		}
	}
	
	public List<Long> sort(long mailboxID, SortKey key) {
		StringBuffer sql = new StringBuffer()
				.append("SELECT m.messageid FROM message m LEFT JOIN physmessage p ON m.physmessageid=p.id WHERE m.mailboxid = ?");
		if (key.match("ARRIVAL")) {
			appendSort(sql, "internaldate", key.isReverse());
		} else if (key.match("DATE")) {
			appendSort(sql, "sentdate", key.isReverse());
		} else if (key.match("FROM")) {
			appendSort(sql, "fromaddr", key.isReverse());
		} else if (key.match("SIZE")) {
			appendSort(sql, "size", key.isReverse());
		} else if (key.match("SUBJECT")) {
			appendSort(sql, "subject", key.isReverse());
		} else {
			// "TO", "CC"
		}
		return (List<Long>) getJdbcTemplate().queryForList(sql.toString(),
				new Object[] { new Long(mailboxID) }, Long.class);
	}
	
	private void appendSort(StringBuffer sql, String field, boolean reverse) {
		sql.append(" ORDER BY p.").append(field);
		if (reverse)
			sql.append(" DESC");
	}

	private List<Long> query(UidToMsnMapper map, long mailboxID, AndKey key) {
		return conjunctionQuery(map, mailboxID, key, true);
	}

	private List<Long> query(UidToMsnMapper map, long mailboxID, OrKey key) {
		return conjunctionQuery(map, mailboxID, key, false);
	}

	private List<Long> query(UidToMsnMapper map, long mailboxID, NotKey key) {
		SearchKey k = key.getSearchKey();
		if (k.isComposite()) {
			return ListUtils.subtract(map.getUIDList(),
					query(map, mailboxID, k));
		} else {
			return ListUtils.subtract(map.getUIDList(), query(map, mailboxID,
					new CompositeKey(k)));
		}
	}
	
	private List<Long> query(UidToMsnMapper map, long mailboxID,
			final HeaderKey key) {
		return query(map, mailboxID, key.getHeaderName(), key.getPattern());
	}
	
	private List<Long> query(UidToMsnMapper map, long mailboxID, String name,
			final String pattern) {
		if (StringUtils.isEmpty(pattern)) {
			String sql = SearchQuery.toQuery(mailboxID, name, true);
			return getJdbcTemplate().queryForList(sql, Long.class);
		} else {
			String sql = SearchQuery.toQuery(mailboxID, name, false);
			final List<Long> results = new ArrayList<Long>();
			getJdbcTemplate().query(sql, new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					// Stored header values are not decoded.
					String value = DecoderUtil.decodeEncodedWords(rs
							.getString(2));
					if (StringUtils.contains(value, pattern)) {
						results.add(rs.getLong(1));
					}
				}
			});
			return results;
		}
	}

	private List<Long> query(UidToMsnMapper map, long mailboxID,
			RecipientStringKey key) {
		return query(map, mailboxID, key.getRecipientType().toString(),
				key.getPattern());
	}

	private List<Long> query(UidToMsnMapper map, long mailboxID, SequenceKey key) {
		List<Long> result = new ArrayList<Long>();
		SequenceRange[] sequenceSet = key.getSequenceSet();
		for (int i = 0; i < sequenceSet.length; i++) {
			long min = map.getMinMessageNumber(sequenceSet[i].getMin());
			long max = map.getMaxMessageNumber(sequenceSet[i].getMax());
			for (long j = min; j <= max && j >= 0; j++) {
				long messageID = map.getUID((int) j);
				if (messageID != -1) {
					result.add(messageID);
				}
			}
		}
		return result;
	}
	
	private List<Long> query(UidToMsnMapper map, long mailboxID, KeywordKey key) {
		String sql = SearchQuery.toQuery(mailboxID, key);
		if (key.getTestSet()) {
			return getJdbcTemplate().queryForList(sql, Long.class);
		} else {
			return ListUtils.subtract(map.getUIDList(), getJdbcTemplate()
					.queryForList(sql, Long.class));
		}
	}

	private List<Long> query(UidToMsnMapper map, long mailboxID, CompositeKey key) {
		String sql = SearchQuery.toQuery(mailboxID, key);
		return getJdbcTemplate().queryForList(sql, Long.class);
	}
	
	private List<Long> conjunctionQuery(UidToMsnMapper map, long mailboxID,
			SearchKeyList key, boolean and) {
		List<Long> result = null;
		List<SearchKey> keys = key.getSearchKeys();
		CompositeKey ck = null;
		for (SearchKey k : keys) {
			if (k.isComposite()) {
				result = query(result, map, mailboxID, k, and);
			} else {
				if (ck == null) {
					ck = new CompositeKey();
				}
				ck.addKey(k);
			}
		}
		if (ck != null) {
			return query(result, map, mailboxID, ck, and);
		} else {
			return result;
		}
	}
	
	private List<Long> query(List<Long> result, UidToMsnMapper map, long mailboxID,
			SearchKey key, boolean and) {
		List<Long> list = query(map, mailboxID, key);
		if (CollectionUtils.isNotEmpty(result)) {
			return (and) ? ListUtils.intersection(result, list) 
						 : ListUtils.sum(result, list);
		} else {
			return list;
		}
	}
	
}
