package com.hs.mail.mailet;

import java.util.Set;

import javax.mail.MessagingException;

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

public abstract class AbstractMailet implements Mailet {

	protected MailetContext context = null;

	public void init(MailetContext context) {
		this.context = context;
	}

	public boolean accept(Set<Recipient> recipients, SmtpMessage message) {
		return false;
	}

	public abstract void service(Set<Recipient> recipients, SmtpMessage message)
			throws MessagingException;

	protected UserManager getUserManager() {
		return context.getUserManager();
	}
	
	protected MailboxManager getMailboxManager() {
		return context.getMailboxManager();
	}

}
