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
package com.hs.mail.imap.processor.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeTokenStream;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class PartContentBuilder {
	
	public static final byte[] EMPTY = {};

	private MimeTokenStream parser;
    private boolean topLevel = true;

	public PartContentBuilder() {
		parser = MimeParser.createDefaultMimeParser();
	}
	
	public void build(InputStream is, int[] path) {
		try {
			parser.setRecursionMode(MimeTokenStream.M_RECURSE);
			parser.parse(is);
	        topLevel = true;
			if (path != null) {
				for (int i = 0; i < path.length; i++) {
					to(path[i]);
				}
			}
		} catch (Exception ex) {
		}
	}
	
	private void skipToStartOfInner(int position) throws IOException,
			MimeException {
		final int state = parser.next();
		switch (state) {
		case MimeTokenStream.T_START_MULTIPART:
			break;
		case MimeTokenStream.T_START_MESSAGE:
			break;
		case MimeTokenStream.T_END_OF_STREAM:
			throw new PartNotFoundException(position);
		case MimeTokenStream.T_END_BODYPART:
			throw new PartNotFoundException(position);
		default:
			skipToStartOfInner(position);
		}
	}

    private void to(int position) throws IOException, MimeException {
		try {
			if (topLevel) {
				topLevel = false;
			} else {
				skipToStartOfInner(position);
			}
			for (int count = 0; count < position;) {
				int state = parser.next();
				if (state == MimeTokenStream.T_START_BODYPART) {
					count++;
				} else if (state == MimeTokenStream.T_BODY && position == 1) {
					count++;
				} else if (state == MimeTokenStream.T_START_MULTIPART) {
					if (count > 0 && count < position) {
						ignore();
					}
				} else if (state == MimeTokenStream.T_END_OF_STREAM) {
					throw new PartNotFoundException(position);
				}
			}
		} catch (IllegalStateException e) {
			throw new PartNotFoundException(position, e);
		}
	}
	
	private void ignore() throws IOException, MimeException {
		for (int state = parser.next(); state != MimeTokenStream.T_END_MULTIPART; state = parser
				.next()) {
			if (state == MimeTokenStream.T_START_MULTIPART) {
				ignore();
				break;
			} else if (state == MimeTokenStream.T_END_OF_STREAM) {
				throw new MimeException("Premature end of stream");
			}
		}
	}
	
	public Map<String, String> getMimeHeader() throws IOException, MimeException {
		Map<String, String> header = new HashMap<String, String>();
		for (int state = parser.getState(); state != MimeTokenStream.T_END_HEADER; 
				state = parser.next()) {
			if (state == MimeTokenStream.T_FIELD) {
				Field field = parser.getField();
				header.put(field.getName(), field.getBody());
			} else if (state == MimeTokenStream.T_END_OF_STREAM) {
				throw new MimeException("Premature end of stream");
			}
		}
		return header;
	}
	
	public Map<String, String> getMessageHeader() throws IOException, MimeException {
		advanceToMessage();
		return getMimeHeader();
	}
	
	private void advanceToMessage() throws IOException, MimeException {
		for (int state = parser.getState(); state != MimeTokenStream.T_START_MESSAGE; 
				state = parser.next()) {
			if (state == MimeTokenStream.T_END_OF_STREAM) {
				throw new MimeException("Premature end of stream");
			}
		}
	}
	
	public byte[] getMimeBodyContent() throws IOException, MimeException {
		parser.setRecursionMode(MimeTokenStream.M_FLAT);
		for (int state = parser.getState(); state != MimeTokenStream.T_BODY
				&& state != MimeTokenStream.T_START_MULTIPART; 
				state = parser.next()) {
			if (state == MimeTokenStream.T_END_OF_STREAM) {
				return EMPTY;
			}
		}
		return IOUtils.toByteArray(parser.getInputStream());
	}
	
	public byte[] getMessageBodyContent() throws IOException, MimeException {
		advanceToMessageBody();
		return getMimeBodyContent();
	}
	
	private void advanceToMessageBody() throws IOException, MimeException {
		for (int state = parser.getState(); state != MimeTokenStream.T_BODY; 
				state = parser.next()) {
			if (state == MimeTokenStream.T_END_OF_STREAM) {
				throw new MimeException("Premature end of stream");
			}
		}
	}
	
	public class PartNotFoundException extends MimeException {

		private static final long serialVersionUID = 1L;
        private final int position;

		public PartNotFoundException(int position) {
			this(position, null);
		}

        public PartNotFoundException(int position, Exception e) {
            super("Part " + position + " not found.", e);
            this.position = position;
        }

        public final int getPosition() {
            return position;
        }

	}

}
