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
package com.hs.mail.imap.processor;

import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.message.request.ImapRequest;

/**
 * <p>
 * IMAP command processor.
 * </p>
 * <p>
 * <strong>Note:</strong> this is a transitional API and is liable to change.
 * </p>
 * 
 * @author Won Chul Doh
 * @since Jan 31, 2010
 */
public interface ImapProcessor {

	/**
	 * Performs processing of the command. If this process does not understand
	 * the given message or failed to process then it must return an appropriate
	 * message as per the specification.
	 * 
	 * @param session
	 * @param message
	 * @param channel
	 */
	public void process(ImapSession session, ImapRequest message,
			Channel channel);

}
