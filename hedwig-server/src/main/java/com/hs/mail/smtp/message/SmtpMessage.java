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
package com.hs.mail.smtp.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.message.MailMessage;
import com.hs.mail.smtp.SmtpException;

/**
 * 
 * @author Won Chul Doh
 * @since May 31, 2010
 *
 */
public class SmtpMessage implements Serializable {

	private static final long serialVersionUID = 7983042396136261548L;
    private static final DateFormat SMTP_DATE_FORMAT = new MailDateFormat();

	public static final int LOCAL = 1;
	public static final int REMOTE = 2;
	public static final int ALL = 3;
	
	/**
     * The number of mails generated.  Access needs to be synchronized for
     * thread safety and to ensure that all threads see the latest value.
     */
    private static long count;
	
	private String name;
	
	private int node;
	/**
	 * The sender of this message.
	 */
	private MailAddress from;
	/**
	 * The remained recipients of this message. 
	 */
	private Set<Recipient> recipients;
	/**
	 * The retry count.
	 */
	private int retryCount = 0;
	/**
	 * The last time this message was updated.
	 */
	private Date lastUpdate = new Date();
	/**
	 * The error message, if any, associated with this mail.
	 */
	private transient String errorMessage = "";
	/**
	 * The MailMessage that holds the mail data.
	 */
	private transient MailMessage mailMessage = null;
	/**
	 * The time this message was created. 
	 */
	private transient long time;

	public SmtpMessage(MailAddress from, int node) {
		this.time = System.currentTimeMillis();
		this.from = from;
		this.recipients = new TreeSet<Recipient>();
		this.name = getId();
		this.node = node;
		this.errorMessage = "";
	}
	
	public SmtpMessage(MailAddress from) {
		this(from, ALL);
	}
	
	public String getName() {
		return name;
	}

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public MailAddress getFrom() {
		return from;
	}
	
	public Set<Recipient> getRecipients() {
		return recipients;
	}

	public void addRecipient(Recipient recipient) {
		recipients.add(recipient);
	}
	
	public int getRecipientsSize() {
		return recipients.size();
	}
	
	public long getTime() {
		return time;
	}

	public int getRetryCount() {
		return retryCount;
	}
	
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getDate() {
		return SMTP_DATE_FORMAT.format(new Date(time));
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isNotificationMessage() {
		return "".equals(getFrom().getMailbox());
	}

	public void appendErrorMessage(String errorMessage) {
		if (this.errorMessage == null)
			this.errorMessage  = errorMessage;
		else
			this.errorMessage += errorMessage;
	}

	public File getDataFile() {
		return new File(Config.getSpoolDirectory(), getName());
	}
	
	private File getControlFile() {
		return new File(Config.getSpoolDirectory(), getName() + ".control");
	}
	
	public void setContent(InputStream is) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(getDataFile());
			IOUtils.copyLarge(is, os);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	public void setContent(MimeMessage msg) throws IOException,
			MessagingException {
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(getDataFile()));
			msg.writeTo(os);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	public void store() throws IOException {
		File file = getControlFile();
		ObjectOutputStream os = null;
		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					file), 1024 * 4);
			os = new ObjectOutputStream(out);
			os.writeObject(this);
			os.flush();
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	public void createTrigger() throws IOException {
		File file = new File(Config.getSnapshotDirectory(), getName());
		file.createNewFile();
	}
	
	public MailMessage getMailMessage() throws IOException {
		if (mailMessage == null) {
			mailMessage = MailMessage.createMailMessage(getDataFile());
		}
		return mailMessage;
	}

	public MimeMessage getMimeMessage() throws MessagingException {
		Session session = Session.getInstance(System.getProperties(), null);
		InputStream is = null;
		try {
			is = new FileInputStream(getDataFile());
			return new MimeMessage(session, is);
		} catch (FileNotFoundException e) {
			throw new MessagingException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void dispose() {
		File file = getDataFile();
		if (file.exists()) {
			if (file.delete()) {
				file = getControlFile();
				if (file.exists()) {
					file.delete();
				}
			}
		}
	}
	
	private String getId() {
		File d = Config.getSpoolDirectory(); 
		File f;
		String id;
		do {
			f = new File(d, (id = _getId()));
		} while (f.exists());
		return id;
	}
	
	private String _getId() {
		long localCount = -1;
		synchronized (SmtpMessage.class) {
			localCount = count++;
		}
		return new StringBuffer(64)
				.append(time)
				.append("-")
				.append(localCount)
				.toString();
	}
	
	public static SmtpMessage readMessage(String name) throws SmtpException {
		File dir = Config.getSpoolDirectory();
		File file = new File(dir, name + ".control");
		ObjectInputStream is = null;
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file),
					1024);
			is = new ObjectInputStream(in);
			SmtpMessage message = (SmtpMessage) is.readObject();
			return message;
		} catch (Exception e) {
			throw new SmtpException("Error while reading message file: " + file);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

}
