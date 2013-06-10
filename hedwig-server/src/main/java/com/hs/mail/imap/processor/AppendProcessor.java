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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.MailMessage;
import com.hs.mail.imap.message.request.AppendRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.HumanReadableText;
import com.hs.mail.imap.processor.fetch.BodyStructureBuilder;
import com.hs.mail.imap.processor.fetch.EnvelopeBuilder;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public class AppendProcessor extends AbstractImapProcessor {

	private BodyStructureBuilder builder = null;
	
	public AppendProcessor() {
		super();
		this.builder = new BodyStructureBuilder(new EnvelopeBuilder());
	}

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) throws Exception {
		AppendRequest request = (AppendRequest) message;
		String mailboxName = request.getMailbox();
		MailboxManager manager = getMailboxManager();
		Mailbox mailbox = manager.getMailbox(session.getUserID(), mailboxName);
		if (mailbox == null) {
			// SHOULD NOT automatically create the mailbox.
			responder.taggedNo(request, "[TRYCREATE]",
					HumanReadableText.MAILBOX_NOT_FOUND);
		} else {
			File temp = File.createTempFile("mail", null, Config.getTempDirectory());
			ChannelBuffer buffer = request.getMessage();
			try {
				writeMessage(buffer, temp);
				MailMessage msg = manager.appendMessage(mailbox.getMailboxID(),
						request.getDatetime(), request.getFlags(), temp);
				// FIXME - cache body structure or not!!!
				// builder.build(msg.getInternalDate(), msg.getPhysMessageID());
			} catch (Exception ex) {
				forceDelete(temp);
				throw ex;
			}
			responder.okCompleted(request);
		}
	}
	
	private void writeMessage(ChannelBuffer buffer, File dst)
			throws IOException {
		ChannelBufferInputStream is = new ChannelBufferInputStream(buffer);
		OutputStream os = null;
		try {
			os = new FileOutputStream(dst);
			IOUtils.copyLarge(is, os);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	private void forceDelete(File file) {
		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			// Don't re-throw this exception
		}
	}	

}
