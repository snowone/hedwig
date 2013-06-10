package com.hs.mail.imap.processor.custom;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.custom.XRevokeRequest;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.processor.AbstractImapProcessor;

/**
 * 
 * @author Won Chul Doh
 * @since Aug 15, 2011
 *
 */
public class XRevokeProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) throws Exception {
		XRevokeRequest request = (XRevokeRequest) message;
		String messageID = request.getMessageID();
		MailboxManager manager = getMailboxManager();
		List<Long> revokedUids = manager.getRevocableMessageIDList(messageID);
		if (CollectionUtils.isNotEmpty(revokedUids)) {
			for (Long uid : revokedUids) {
				manager.deleteMessage(uid);				
			}
		}
		responder.okCompleted(request);
	}

}
