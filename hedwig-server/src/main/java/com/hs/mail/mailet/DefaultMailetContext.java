package com.hs.mail.mailet;

import java.io.FileInputStream;
import java.io.IOException;

import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.smtp.message.MailAddress;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;
import com.hs.mail.util.MailUtils;

public class DefaultMailetContext implements MailetContext {
	
	private MailboxManager mailboxManager = null;
	private UserManager userManager = null;
	
	public MailboxManager getMailboxManager() {
		return mailboxManager;
	}

	public void setMailboxManager(MailboxManager manager) {
		this.mailboxManager = manager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager manager) {
		this.userManager = manager;
	}

	public void sendMail(String sender, String[] recipients, SmtpMessage msg)
			throws IOException {
		SmtpMessage message = prepare(sender, recipients);
		try {
			message.setContent(new FileInputStream(msg.getDataFile()));
			message.store();
			message.createTrigger();
		} catch (IOException e) {
			message.dispose();
		}
	}
	
	private SmtpMessage prepare(String sender, String[] recipients) {
		SmtpMessage message = new SmtpMessage(new MailAddress(sender, false));
		for (String recipient : recipients) {
			message.addRecipient(new Recipient(recipient, false));
		}
		return message;
	}
	
	public void storeMail(long soleRecipientID, String destination,
			SmtpMessage msg) throws IOException {
		mailboxManager.addMessage(soleRecipientID, msg.getMailMessage(),
				MailUtils.encodeMailbox(destination));
	}
	
}
