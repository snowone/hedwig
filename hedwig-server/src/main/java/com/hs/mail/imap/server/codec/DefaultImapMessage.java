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
package com.hs.mail.imap.server.codec;

import java.io.StringReader;
import java.util.LinkedList;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.hs.mail.imap.parser.CommandParser;
import com.hs.mail.imap.parser.Token;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 22, 2010
 *
 */
public class DefaultImapMessage implements ImapMessage {

    private ChannelBuffer literal = ChannelBuffers.EMPTY_BUFFER;
	private long literalLength = -1;
	private boolean needContinuationRequest = false;
	protected LinkedList<Token> tokens = null;
	
    public DefaultImapMessage(String request) {
		CommandParser parser = new CommandParser(new StringReader(request));
		tokens = parser.command();
		if (CollectionUtils.isNotEmpty(tokens)) {
			Token last = tokens.get(tokens.size() - 1);
			if (last.isLiteral()) {
				int length = Integer.parseInt(last.value);
				setLiteralLength(length);
				if (Token.Type.LITERAL == last.type) {
					needContinuationRequest = true;
				}
			}
		}
	}
    
	public String getCommand() {
		return (tokens.size() > 0) ? tokens.get(1).value : null;
	}
    
    public LinkedList<Token> getTokens() {
		return tokens;
	}

	public ChannelBuffer getLiteral() {
		return literal;
	}

	public void setLiteral(ChannelBuffer literal) {
		if (null == literal) {
			literal = ChannelBuffers.EMPTY_BUFFER;
		}
		this.literal = literal;
	}

	public long getLiteralLength() {
		return literalLength;
	}

	public void setLiteralLength(long literalLength) {
		this.literalLength = literalLength;
	}

	public boolean isNeedContinuationRequest() {
		return needContinuationRequest;
	}

}
