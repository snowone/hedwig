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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.hs.mail.imap.message.request.ImapRequest;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 1, 2010
 *
 */
public interface Responder {

	public Channel getChannel();
	
	public ImapRequest getRequest();

	public ChannelFuture okCompleted(ImapRequest request);
	
	public ChannelFuture okCompleted(ImapRequest request, String responseCode);

	public ChannelFuture bye(String text);

	public ChannelFuture taggedNo(ImapRequest request, String text);

	public ChannelFuture taggedNo(ImapRequest request, String responseCode,
			String text);

	public ChannelFuture untaggedOK(String text);

	public ChannelFuture untagged(String text);

}
