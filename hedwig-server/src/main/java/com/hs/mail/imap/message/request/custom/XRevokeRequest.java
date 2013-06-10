package com.hs.mail.imap.message.request.custom;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.ImapSession.State;
import com.hs.mail.imap.message.request.ImapRequest;

/**
 * 
 * @author Won Chul Doh
 * @since Aug 15, 2011
 *
 */
public class XRevokeRequest extends ImapRequest {

	private final String messageID;
	
	public XRevokeRequest(String tag, String command, String messageID) {
		super(tag, command);
		this.messageID = messageID;
	}
	
	public String getMessageID() {
		return messageID;
	}

	@Override
	public boolean validForState(State state) {
		return state == ImapSession.State.SELECTED;
	}

}
