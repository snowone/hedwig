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
package com.hs.mail.imap.message.responder;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.mail.Flags;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.springframework.util.CollectionUtils;

import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.response.ImapResponse;
import com.hs.mail.imap.processor.fetch.Content;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 17, 2010
 *
 */
public abstract class AbstractImapResponder implements Responder {
	
	protected Channel channel;
	protected ImapRequest request;
	
	private boolean skipNextSpace;

	protected AbstractImapResponder(Channel channel, ImapRequest request) {
		super();
		this.channel = channel;
		this.request = request;
		this.skipNextSpace = false;
	}

	public Channel getChannel() {
		return channel;
	}
	
	public ImapRequest getRequest() {
		return request;
	}
	
	ChannelFuture write(Object message) {
		return channel.write(message);
	}

	public ChannelFuture okCompleted(ImapRequest request) {
		return write(request.getTag() + " OK " + request.getCommand()
				+ " completed\r\n");
	}
	
	public ChannelFuture okCompleted(ImapRequest request, String responseCode) {
		return write(request.getTag() + " OK " + responseCode + " "
				+ request.getCommand() + " completed\r\n");
	}

	public ChannelFuture bye(String text) {
		return write("* BYE " + text + "\r\n");
	}

	public ChannelFuture taggedNo(ImapRequest request, String text) {
		return write(request.getTag() + " NO " + request.getCommand()
				+ " failed. " + text + "\r\n");
	}

	public ChannelFuture taggedNo(ImapRequest request, String responseCode,
			String text) {
		return write(request.getTag() + " NO " + responseCode + " "
				+ request.getCommand() + " failed. " + text + "\r\n");
	}

	public ChannelFuture untaggedOK(String text) {
		return write("* OK " + text + "\r\n");
	}

	public ChannelFuture untagged(String text) {
		return write("* " + text);
	}

	protected void message(String message) {
		space();
		write(message);
	}
	
	protected void message(long number) {
		space();
		write(Long.toString(number));
	}
	
	protected void space() {
		if (skipNextSpace)
			skipNextSpace = false;
		else
			write(" ");
	}
	
	protected void skipNextSpace() {
		skipNextSpace = true;
	}
	
	protected void openParen(String bracket) {
		space();
		write(bracket);
		skipNextSpace();
	}
	
	protected void closeParen(String bracket) {
		write(bracket);
	}
	
	protected void end() {
		write("\r\n");
	}
	
	protected void quote(String message) {
		message("\"" + StringUtils.replace(message, "\"", "\\\"") + "\"");
	}
	
	protected void quoteUpper(String message) {
		quote(message.toUpperCase());
	}
	
	protected void nil() {
		message(ImapResponse.NIL);
	}
	
	protected void nillableQuote(String message) {
		if (null == message) {
			nil();
		} else {
			quote(message);
		}
	}
	
	protected void nillableQuotes(List<String> quotes) {
		if (CollectionUtils.isEmpty(quotes)) {
			nil();
		} else {
			openParen("(");
			for (String message : quotes) {
				nillableQuote(message);
			}
			closeParen(")");
		}
	}
	
	protected void flags(Flags flags) {
		if (flags.contains(Flags.Flag.ANSWERED)) {
			message("\\Answered");
		}
		if (flags.contains(Flags.Flag.DELETED)) {
			message("\\Deleted");
		}
		if (flags.contains(Flags.Flag.DRAFT)) {
			message("\\Draft");
		}
		if (flags.contains(Flags.Flag.FLAGGED)) {
			message("\\Flagged");
		}
		if (flags.contains(Flags.Flag.RECENT)) {
			message("\\Recent");
		}
		if (flags.contains(Flags.Flag.SEEN)) {
			message("\\Seen");
		}
		String[] ufs = flags.getUserFlags();
		if (!ArrayUtils.isEmpty(ufs)) {
			for (String uf : ufs) {
				message(uf);
			}
		}
		skipNextSpace = false;
	}
	
	protected String encodeDateTime(Date date) {
		FastDateFormat df = FastDateFormat.getInstance(
				"dd-MMM-yyyy HH:mm:ss Z", TimeZone.getTimeZone("GMT"),
				Locale.US);
		return df.format(date);
	}
	
	protected void literal(Content content) {
		long size = content.getSize();
		message("{" + size + "}\r\n");
		if (size > 0) {
			ByteBuffer contents = content.getContents();
			write(contents);
		}
	}

}
