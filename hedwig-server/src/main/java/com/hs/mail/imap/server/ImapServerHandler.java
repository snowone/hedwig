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
package com.hs.mail.imap.server;

import java.nio.channels.ClosedChannelException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.message.ImapRequestFactory;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.response.HumanReadableText;
import com.hs.mail.imap.processor.ImapProcessor;
import com.hs.mail.imap.processor.ImapProcessorFactory;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * Handles a server-side IMAP channel.
 * 
 * @author Won Chul Doh
 * @since Jan 12, 2010
 */

public class ImapServerHandler extends SimpleChannelUpstreamHandler {

	private static Logger logger = Logger.getLogger(ImapServerHandler.class);

	private static final boolean throttleIO = true;
	
	public ImapServerHandler() {
		super();
		ImapProcessorFactory.registerProcessors();
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		if (e != null) {
			if (e instanceof ChannelStateEvent) {
				if (logger.isDebugEnabled()) {
					logger.debug(e.toString());
				}
			}
			super.handleUpstream(ctx, e);
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// Send greeting for a new connection.
		e.getChannel().write(HumanReadableText.GREETINGS);
		ctx.setAttachment(new ImapSession());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		ImapMessage message = (ImapMessage) e.getMessage();
		if (throttleIO) {
			if (!e.getChannel().isWritable()) {
				e.getChannel().setReadable(false);
			}
		}
		ImapSession session = (ImapSession) ctx.getAttachment();
		ImapRequest request = ImapRequestFactory.createImapRequest(message);
		ImapProcessor processor = ImapProcessorFactory
				.createImapProcessor(request);
		processor.process(session, request, e.getChannel());
	}
	
	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		e.getChannel().setReadable(true);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.log(Level.WARN, "Exception from downstream.", e.getCause());
		if (e.getCause() instanceof ReadTimeoutException) {
			e.getChannel().close();
		} else if (e.getCause() instanceof ClosedChannelException) {
			e.getChannel().close();
		} else {
			e.getChannel().write(e.getCause().getMessage() + "\r\n");
		}
	}

}
