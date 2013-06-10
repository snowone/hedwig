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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.mail.FetchProfile;

import com.hs.mail.imap.message.BodyFetchItem;
import com.hs.mail.imap.message.FetchData;
import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.FetchRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 29, 2010
 *
 */
public class FetchRequestBuilder extends AbstractUidRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message, boolean useUID) {
		LinkedList<Token> tokens = message.getTokens();
		SequenceRange[] sequenceSet = parseSequenceSet(tokens);
		FetchProfile fp = decodeFetchData(tokens);
		if (useUID && !fp.contains(FetchData.FetchProfileItem.UID)) {
			// RFC2060 says that a UID FETCH must include a UID in the response
			// even if the fetch did not ask for it.
			fp.add(FetchData.FetchProfileItem.UID);
		}
		return new FetchRequest(tag, command, sequenceSet, fp, useUID);
	}
	
	private String parseSection(LinkedList<Token> tokens, BodyFetchItem item) {
		StringBuilder sb = new StringBuilder();
		tokens.remove(); // consume '['
		Token token = tokens.remove();
		if (token.type == Token.Type.NUMBER) {
			item.setSectionType(BodyFetchItem.CONTENT);
			do {
				item.addPath(Integer.parseInt(token.value));
				sb.append(token.value);
				if (!".".equals((token = tokens.remove()).value))
					break;
				sb.append(".");
			} while ((token = tokens.remove()).type == Token.Type.NUMBER);
		}
		if (!"]".equals(token.value)) {
			sb.append(token.value);
			if ("HEADER.FIELDS.NOT".equals(token.value)
					|| "HEADER.FIELDS".equals(token.value)) {
				item.setSectionType(
						"HEADER.FIELDS".equals(token.value) ? BodyFetchItem.HEADER_FIELDS
								: BodyFetchItem.HEADER_FIELDS_NOT);
				item.setHeaders(parseHeaderList(sb, tokens));
			} else if ("HEADER".equals(token.value)) {
				item.setSectionType(BodyFetchItem.HEADER);
			} else if ("TEXT".equals(token.value)) {
				item.setSectionType(BodyFetchItem.TEXT);
			} else if ("MIME".equals(token.value)) {
				item.setSectionType(BodyFetchItem.MIME);
			}
			token = tokens.remove(); // consume ']'
			assert "]".equals(token.value);
		} else {
			// An empty section specification refers to the entire message,
			// including the header.
			item.setSectionType(BodyFetchItem.CONTENT);
		}
		return sb.toString();
	}
	
	private String[] parseHeaderList(StringBuilder sb, LinkedList<Token> tokens) {
		Token token = tokens.peek();
		List<String> headers = new ArrayList<String>();
		if (token.type == Token.Type.LPAREN) {
			sb.append(" (");
			tokens.remove(); // consume '('
			do {
				if ((token = tokens.remove()).type == Token.Type.RPAREN) {
					sb.append(')');
					break;
				}
				if (headers.size() > 0)
					sb.append(' ');
				headers.add(token.value);
				sb.append(token.value);
			} while (true);
		}
		return headers.toArray(new String[headers.size()]);
	}
	
	private void parsePartial(LinkedList<Token> tokens, BodyFetchItem item) {
		Token token = tokens.peek();
		if ("<".equals(token.value)) {
			tokens.remove(); // consume '<'
			item.setFirstOctet(Long.parseLong((token = tokens.remove()).value));
			token = tokens.remove(); // consume '.'
			assert ".".equals(token.value);
			item.setNumberOfOctets(Long
					.parseLong((token = tokens.remove()).value));
			token = tokens.remove(); // consume '>'
			assert ">".equals(token.value);
		}
	}
	
	private FetchProfile decodeFetchData(LinkedList<Token> tokens) {
		FetchProfile fp = new FetchProfile();
		Token token = tokens.peek();
		if (token.type == Token.Type.LPAREN) {
			tokens.remove(); // consume '('
			do {
				if ((token = tokens.peek()).type == Token.Type.RPAREN) {
					tokens.remove(); // consume ')'
					break;
				}
				decodeFetchAtt(tokens, fp);
			} while (true);
		} else {
			decodeFetchAtt(tokens, fp);
		}
		return fp;
	}
	
	private FetchProfile decodeFetchAtt(LinkedList<Token> tokens,
			FetchProfile fp) {
		String value = (tokens.remove()).value.toUpperCase();
		Token next = tokens.peek();
		if (next == null || !"[".equals(next.value)) {
			if ("ALL".equals(value)) {
				// equivalent to: (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE)
				fp.add(FetchProfile.Item.FLAGS);
				fp.add(FetchData.FetchProfileItem.INTERNALDATE);
				fp.add(FetchData.FetchProfileItem.SIZE);
				fp.add(FetchProfile.Item.ENVELOPE);
			} else if ("FAST".equals(value)) {
				// equivalent to: (FLAGS INTERNALDATE RFC822.SIZE)
				fp.add(FetchProfile.Item.FLAGS);
				fp.add(FetchData.FetchProfileItem.INTERNALDATE);
				fp.add(FetchData.FetchProfileItem.SIZE);
			} else if ("FULL".equals(value)) {
				// equivalent to: (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE BODY)
				fp.add(FetchProfile.Item.FLAGS);
				fp.add(FetchData.FetchProfileItem.INTERNALDATE);
				fp.add(FetchData.FetchProfileItem.SIZE);
				fp.add(FetchProfile.Item.ENVELOPE);
				fp.add(FetchData.FetchProfileItem.BODY);
			} else if ("FLAGS".equals(value)) {
				fp.add(FetchProfile.Item.FLAGS);
			} else if ("INTERNALDATE".equals(value)) {
				fp.add(FetchData.FetchProfileItem.INTERNALDATE);
			} else if ("ENVELOPE".equals(value)) {
				fp.add(FetchProfile.Item.ENVELOPE);
			} else if ("RFC822".equals(value)) {
				// equivalent to: BODY[]
				fp.add(new BodyFetchItem("RFC822", false, BodyFetchItem.CONTENT));
			} else if ("RFC822.HEADER".equals(value)) {
				// equivalent to: BODY.PEEK[HEADER]
				fp.add(new BodyFetchItem("RFC822.HEADER", true, BodyFetchItem.HEADER));
			} else if ("RFC822.SIZE".equals(value)) {
				fp.add(FetchData.FetchProfileItem.SIZE);
			} else if ("RFC822.TEXT".equals(value)) {
				// equivalent to: BODY[TEXT]
				fp.add(new BodyFetchItem("RFC822.TEXT", false, BodyFetchItem.TEXT));
			} else if ("BODY".equals(value)) {
				fp.add(FetchData.FetchProfileItem.BODY);
			} else if ("BODYSTRUCTURE".equals(value)) {
				fp.add(FetchData.FetchProfileItem.BODYSTRUCTURE);
			} else if ("UID".equals(value)) {
				fp.add(FetchData.FetchProfileItem.UID);
			}
		} else {
			// BODY || BODY.PEEK
			BodyFetchItem body = new BodyFetchItem("BODY", !"BODY".equals(value));
			String parameter = parseSection(tokens, body);
			parsePartial(tokens, body);
			body.setName("BODY[" + parameter + "]");
			fp.add(body);
		}
		return fp;
	}

}
