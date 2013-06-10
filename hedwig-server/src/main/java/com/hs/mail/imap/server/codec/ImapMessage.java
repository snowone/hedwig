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

import java.util.LinkedList;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.hs.mail.imap.parser.Token;

/**
 * An IMAP message.
 * 
 * @author Won Chul Doh
 * @since Jan 22, 2010
 * 
 */
public interface ImapMessage {

	public String getCommand();
	
	LinkedList<Token> getTokens();
	
	/**
	 * Returns the content of this message. If there is no content, an
	 * {@link ChannelBuffers#EMPTY_BUFFER} is returned.
	 */
	ChannelBuffer getLiteral();

	/**
	 * Sets the content of this message. If {@code null} is specified, the
	 * content of this message will be set to
	 * {@link ChannelBuffers#EMPTY_BUFFER}.
	 */
	void setLiteral(ChannelBuffer literal);

    /**
     * Returns the length of the content.  Please note that this value is
     * not retrieved from {@link #getContent()} but from the
     * {@code "Content-Length"} header, and thus they are independent from each
     * other.
     *
     * @return the content length or {@code 0} if this message does not have
     *         the {@code "Content-Length"} header
     */
    long getLiteralLength();

    void setLiteralLength(long literalLength);
    
	/**
	 * Returns {@code true} if and only if we need to send command continuation
	 * request before reading literal data.
	 */
	boolean isNeedContinuationRequest();
	
}
