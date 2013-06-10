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
package com.hs.mail.imap.message.builder;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.internet.MimeUtility;
import javax.mail.search.ComparisonTerm;

import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.SearchRequest;
import com.hs.mail.imap.message.search.AllKey;
import com.hs.mail.imap.message.search.AndKey;
import com.hs.mail.imap.message.search.FlagKey;
import com.hs.mail.imap.message.search.FromStringKey;
import com.hs.mail.imap.message.search.HeaderKey;
import com.hs.mail.imap.message.search.InternalDateKey;
import com.hs.mail.imap.message.search.KeywordKey;
import com.hs.mail.imap.message.search.NotKey;
import com.hs.mail.imap.message.search.OrKey;
import com.hs.mail.imap.message.search.RecipientStringKey;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SearchKeyList;
import com.hs.mail.imap.message.search.SentDateKey;
import com.hs.mail.imap.message.search.SequenceKey;
import com.hs.mail.imap.message.search.SizeKey;
import com.hs.mail.imap.message.search.SubjectKey;
import com.hs.mail.imap.message.search.TextKey;
import com.hs.mail.imap.parser.ParseException;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.DecoderUtils;
import com.hs.mail.imap.server.codec.ImapMessage;
import com.hs.mail.util.MailUtils;

/**
 * 
 * @author Won Chul Doh
 * @since 30 Jan, 2010
 *
 */
public class SearchRequestBuilder extends AbstractUidRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message, boolean useUID) {
		LinkedList<Token> tokens = message.getTokens();
		String charset = parseCharset(tokens);
		SearchKey searchKey = createSearchKey(tag, tokens, charset);
		return new SearchRequest(tag, command, charset, searchKey, useUID);
	}
	
	protected SearchKey createSearchKey(String tag, LinkedList<Token> tokens,
			String charset) {
		try {
			SearchKeyList searchKey = new AndKey();
			do {
				searchKey.addKey(decodeSearchKey(tokens, charset));
			} while (tokens.peek() != null);
			removeAllKey(searchKey);
			if (searchKey.size() == 1) {
				return searchKey.getSearchKeys().get(0);
			} else {
				return searchKey;
			}
		} catch (Exception e) {
			throw new ParseException(tag, "Error while parsing command");
		}
	}
	
	private void removeAllKey(SearchKeyList searchKey) {
		if (searchKey.size() > 1) {
			searchKey.getSearchKeys().remove(new AllKey());
		}
	}

	protected String parseCharset(LinkedList<Token> tokens) {
		Token token = tokens.peek();
		if ("CHARSET".equals(token.value.toUpperCase())) {
			tokens.remove();
			return MimeUtility.javaCharset(tokens.remove().value);
		} else {
			return null;
		}
	}
	
	private SearchKey decodeSearchKey(LinkedList<Token> tokens, String charset)
			throws Exception {
		Token token = tokens.peek();
		SearchKey key = null;
		if (token.type == Token.Type.KEYWORD) {
			tokens.remove();
			String value = token.value.toUpperCase();
			if ("ALL".equals(value)) {
				key = new AllKey();
			} else if ("ANSWERED".equals(value)) {
				key = new FlagKey(Flags.Flag.ANSWERED, true);
			} else if ("BCC".equals(value)) {
				key = new RecipientStringKey(Message.RecipientType.BCC, decode(
						(token = tokens.remove()).value, charset));
			} else if ("BEFORE".equals(value)) {
				key = new InternalDateKey(ComparisonTerm.LT, DecoderUtils
						.parseDate((token = tokens.remove()).value));
			} else if ("BODY".equals(value)) {
				key = new TextKey(decode((token = tokens.remove()).value,
						charset), false);
			} else if ("CC".equals(value)) {
				key = new RecipientStringKey(Message.RecipientType.CC, decode(
						(token = tokens.remove()).value, charset));
			} else if ("DELETED".equals(value)) {
				key = new FlagKey(Flags.Flag.DELETED, true);
			} else if ("FLAGGED".equals(value)) {
				key = new FlagKey(Flags.Flag.FLAGGED, true);
			} else if ("FROM".equals(value)) {
				key = new FromStringKey(decode((token = tokens.remove()).value,
						charset));
			} else if ("KEYWORD".equals(value)) {
				key = new KeywordKey(decode((token = tokens.remove()).value,
						charset), true);
			} else if ("NEW".equals(value)) {
				key = new AndKey(new FlagKey(Flags.Flag.RECENT, true),
						new FlagKey(Flags.Flag.SEEN, false));
			} else if ("OLD".equals(value)) {
				key = new FlagKey(Flags.Flag.RECENT, false);
			} else if ("ON".equals(value)) {
				key = new InternalDateKey(ComparisonTerm.EQ, DecoderUtils
						.parseDate((token = tokens.remove()).value));
			} else if ("RECENT".equals(value)) {
				key = new FlagKey(Flags.Flag.RECENT, true);
			} else if ("SEEN".equals(value)) {
				key = new FlagKey(Flags.Flag.SEEN, true);
			} else if ("SINCE".equals(value)) {
				key = new InternalDateKey(ComparisonTerm.GE, DecoderUtils
						.parseDate((token = tokens.remove()).value));
			} else if ("SUBJECT".equals(value)) {
				key = new SubjectKey(decode((token = tokens.remove()).value,
						charset));
			} else if ("TEXT".equals(value)) {
				key = new TextKey((token = tokens.remove()).value);
			} else if ("TO".equals(value)) {
				key = new RecipientStringKey(Message.RecipientType.TO, decode(
						(token = tokens.remove()).value, charset));
			} else if ("UNANSWERED".equals(value)) {
				key = new FlagKey(Flags.Flag.ANSWERED, false);
			} else if ("UNDELETED".equals(value)) {
				key = new FlagKey(Flags.Flag.DELETED, false);
			} else if ("UNFLAGGED".equals(value)) {
				key = new FlagKey(Flags.Flag.FLAGGED, false);
			} else if ("UNKEYWORD".equals(value)) {
				key = new KeywordKey((token = tokens.remove()).value, false);
			} else if ("UNSEEN".equals(value)) {
				key = new FlagKey(Flags.Flag.SEEN, false);
			} else if ("DRAFT".equals(value)) {
				key = new FlagKey(Flags.Flag.DRAFT, true);
			} else if ("HEADER".equals(value)) {
				String headerName = tokens.remove().value;
				key = new HeaderKey(headerName, (token = tokens.remove()).value);
			} else if ("LARGER".equals(value)) {
				key = new SizeKey(ComparisonTerm.GT, Integer
						.parseInt((token = tokens.remove()).value));
			} else if ("NOT".equals(value)) {
				key = new NotKey(decodeSearchKey(tokens, charset));
			} else if ("OR".equals(value)) {
				SearchKey k1 = decodeSearchKey(tokens, charset);
				SearchKey k2 = decodeSearchKey(tokens, charset);
				key = new OrKey(k1, k2);
			} else if ("SENTBEFORE".equals(value)) {
				key = new SentDateKey(ComparisonTerm.LT, DecoderUtils
						.parseDate((token = tokens.remove()).value));
			} else if ("SENTON".equals(value)) {
				key = new SentDateKey(ComparisonTerm.EQ, DecoderUtils
						.parseDate((token = tokens.remove()).value));
			} else if ("SENTSINCE".equals(value)) {
				key = new SentDateKey(ComparisonTerm.GE, DecoderUtils
						.parseDate((token = tokens.remove()).value));
			} else if ("SMALLER".equals(value)) {
				key = new SizeKey(ComparisonTerm.LT, Integer
						.parseInt((token = tokens.remove()).value));
			} else if ("UID".equals(value)) {
				SequenceRange[] sequenceSet = parseSequenceSet(tokens);
				key = new SequenceKey(sequenceSet, true);
			} else if ("UNDRAFT".equals(value)) {
				key = new FlagKey(Flags.Flag.DRAFT, false);
			}
		} else if (token.type == Token.Type.SEQ_NUMBER
				|| token.type == Token.Type.SEQ_RANGE) {
			SequenceRange[] sequenceSet = parseSequenceSet(tokens);
			key = new SequenceKey(sequenceSet);
		} else if (token.type == Token.Type.LPAREN) {
			tokens.remove();
			key = new AndKey();
			do {
				if (token.type == Token.Type.RPAREN) {
					tokens.remove();
					break;
				}
				((AndKey) key).addKey(decodeSearchKey(tokens, charset));
				token = tokens.peek();
			} while (true);
		} else {
			throw new Exception("Unexpected token: " + token);
		}
		return key;
	}
	
	private static String decode(String s, String charset) {
		if (charset != null) {
			try {
				if (!MailUtils.isAscii(s) && !"ISO8859_1".equals(charset)) {
					return new String(s.getBytes("ISO8859_1"), charset);
				}
			} catch (UnsupportedEncodingException e) {
			}
		}
		return s;
	}

}
