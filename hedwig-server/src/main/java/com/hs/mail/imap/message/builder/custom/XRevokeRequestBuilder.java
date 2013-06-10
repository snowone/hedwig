package com.hs.mail.imap.message.builder.custom;

import com.hs.mail.imap.message.builder.ImapRequestBuilder;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.custom.XRevokeRequest;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Aug 15, 2011
 *
 */
public class XRevokeRequestBuilder extends ImapRequestBuilder {

	@Override
	public ImapRequest createRequest(String tag, String command,
			ImapMessage message) {
		String messageID = message.getTokens().remove().value;
		return new XRevokeRequest(tag, command, messageID);
	}

}
