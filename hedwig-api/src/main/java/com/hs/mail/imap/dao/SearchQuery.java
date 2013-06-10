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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.hs.mail.imap.message.search.ComparisonKey;
import com.hs.mail.imap.message.search.CompositeKey;
import com.hs.mail.imap.message.search.DateKey;
import com.hs.mail.imap.message.search.FlagKey;
import com.hs.mail.imap.message.search.FromStringKey;
import com.hs.mail.imap.message.search.IntegerComparisonKey;
import com.hs.mail.imap.message.search.InternalDateKey;
import com.hs.mail.imap.message.search.KeywordKey;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SentDateKey;
import com.hs.mail.imap.message.search.SizeKey;
import com.hs.mail.imap.message.search.StringKey;
import com.hs.mail.imap.message.search.SubjectKey;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 8, 2010
 *
 */
class SearchQuery {
	
	static String toQuery(long mailboxID, CompositeKey key) {
		StringBuilder sql = new StringBuilder(
				"SELECT messageid FROM message m, physmessage p WHERE m.mailboxid=")
				.append(mailboxID).append(" AND m.physmessageid=p.id");
		List<SearchKey> keys = key.getSearchKeys();
		String s = null;
		for (SearchKey k : keys) {
			if ((s = toQuery(k)) != null) {
				sql.append(" AND ").append(s);
			}
		}
		return sql.toString();
	}

	static String toQuery(long mailboxID, String headername, boolean emptyValue) {
		if (emptyValue) {
			return String
					.format("SELECT messageid FROM message m JOIN physmessage p ON m.physmessageid=p.id JOIN headervalue v ON v.physmessageid=p.id JOIN headername n ON v.headernameid=n.id  WHERE mailboxid=%d AND headername='%s'",
							mailboxID, headername);
		} else {
			return String
					.format("SELECT m.messageid, v.headervalue FROM message m JOIN physmessage p ON m.physmessageid=p.id JOIN headervalue v ON v.physmessageid=p.id JOIN headername n ON v.headernameid=n.id  WHERE mailboxid=%d AND headername='%s'",
							mailboxID, headername);
		}
	}

	static String toQuery(long mailboxID, KeywordKey key) {
		return String
				.format(
						"SELECT m.messageid FROM message m, keyword k WHERE m.mailboxid=%d AND m.messageid=k.messageid AND k.keyword='%s'",
						mailboxID, key.getPattern());
	}

	private static String toQuery(SearchKey key) {
		if (key instanceof FlagKey) {
			return flagQuery((FlagKey) key);
		} else if (key instanceof FromStringKey) {
			return stringQuery("fromaddr", (FromStringKey) key);
		} else if (key instanceof InternalDateKey) {
			return dateQuery("internaldate", (InternalDateKey) key);
		} else if (key instanceof SentDateKey) {
			return dateQuery("sentdate", (SentDateKey) key);
		} else if (key instanceof SizeKey) {
			return numberQuery("size", (SizeKey) key);
		} else if (key instanceof SubjectKey) {
			return stringQuery("subject", ((SubjectKey) key));
		} else { // AllKey
			return null;
		}
	}

	private static String flagQuery(FlagKey key) {
		String s = FlagUtils.getFlagColumnName(key.getFlag());
		if (StringUtils.isNotEmpty(s)) {
			String v = key.isSet() ? "Y" : "N";
			return new StringBuilder(s).append("='").append(v).append("'")
					.toString();
		} else {
			return null;
		}
	}
	
	private static String dateQuery(String field, DateKey key) {
		return String.format("DATE(%s) %s DATE('%s')", field, getOp(key
				.getComparison()), DateFormatUtils.ISO_DATE_FORMAT.format(key
				.getDate()));
	}
	
	private static String stringQuery(String field, StringKey key) {
		return String.format("%s LIKE '%%%s%%'", field, key.getPattern());
	}
	
	private static String numberQuery(String field, IntegerComparisonKey key) {
		return String.format("%s %s %d", field, getOp(key.getComparison()), key
				.getNumber());
	}

	private static String getOp(int comparison) {
		switch (comparison) {
		case ComparisonKey.LE:
			return "<=";
		case ComparisonKey.LT:
			return "<";
		case ComparisonKey.EQ:
			return "=";
		case ComparisonKey.NE:
			return "!=";
		case ComparisonKey.GT:
			return ">";
		case ComparisonKey.GE:
		default:
			return ">=";
		}
	}
	
}
