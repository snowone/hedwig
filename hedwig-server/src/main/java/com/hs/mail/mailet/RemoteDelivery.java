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
package com.hs.mail.mailet;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.hs.mail.container.config.ComponentManager;
import com.hs.mail.container.config.Config;
import com.hs.mail.dns.DnsServer;
import com.hs.mail.smtp.message.HostAddress;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;
import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * Receives a Mail from SmtpMessageConsumer and takes care of delivery of the
 * message to remote hosts. If for some reason mail can't be delivered store it
 * with undelivered recipients and current retry count in the "spool"
 * repository. After the next "retryDelayTime" the SpoolRepository will try to
 * send it again. After "maxRetries" the mail will be considered undeliverable
 * and will be returned to sender.
 * 
 * @author Won Chul Doh
 * @since Jun 7, 2010
 * 
 */
public class RemoteDelivery extends AbstractMailet {
	
	static Logger logger = Logger.getLogger(RemoteDelivery.class);
	
	private Session session = null;
	private boolean debug = false;
    private int maxRetries = 3; // default number of retries
    private long smtpTimeout = 5000;  //default number of ms to timeout on smtp delivery
    private boolean sendPartial = true; // If false then ANY address errors will cause the transmission to fail
    private long connectionTimeout = 60000;  // The amount of time JavaMail will wait before giving up on a socket connect()
    private String gateway = null;	// The server to send all email to
    private String authUser = null; // auth for gateway server
    private String authPass = null; // password for gateway server

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Initialize the mailet
	 */
	public void init(MailetContext context) {
		super.init(context);
		Properties props = new Properties();
		Properties sysprops = System.getProperties();
		for (Object key : sysprops.keySet()) {
			if (((String) key).startsWith("mail.")) {
				props.put(key, sysprops.get(key));
			}
		}
		if (this.debug)
			props.setProperty("mail.debug", "true");
		props.setProperty("mail.smtp.timeout", smtpTimeout + "");
        props.setProperty("mail.smtp.connectiontimeout", connectionTimeout + "");
        props.setProperty("mail.smtp.sendpartial", String.valueOf(sendPartial));
        props.setProperty("mail.smtp.localhost", Config.getHelloName());
        
        this.maxRetries = (int) Config.getNumberProperty("max_retry_count", 3); 
        
		this.session = Session.getInstance(props, null);
		this.session.setDebug(this.debug);
		
		this.gateway = Config.getProperty("smtp_gateway", null);
		this.authUser = Config.getProperty("smtp_gateway_username", null);
		this.authPass = Config.getProperty("smtp_gateway_password", null);
	}

	public boolean accept(Set<Recipient> recipients, SmtpMessage message) {
		return message.getNode() == SmtpMessage.REMOTE
				|| message.getNode() == SmtpMessage.ALL;
	}

	/**
	 * For this message, we take the list of recipients, organize these into
	 * distinct servers, and deliver the message for each of these servers.
	 */
	public void service(Set<Recipient> recipients, SmtpMessage message)
			throws MessagingException {
		// Organize the recipients into distinct servers (name made case insensitive)
		Hashtable<String, Collection<Recipient>> targets = new Hashtable<String, Collection<Recipient>>();
		for (Recipient recipient : recipients) {
			String targetServer = recipient.getHost().toLowerCase(Locale.US);
			if (!Config.isLocal(targetServer)) {
				String key = (null == gateway) ? targetServer : gateway;
				Collection<Recipient> temp = targets.get(key);
				if (temp == null) {
					temp = new ArrayList<Recipient>();
					targets.put(key, temp);
				}
				temp.add(recipient);
			}
		}
		
		if (targets.isEmpty()) {
			return;
		}
		
		// We have the recipients organized into distinct servers
		List<Recipient> undelivered = new ArrayList<Recipient>();
		MimeMessage mimemsg = message.getMimeMessage();
		boolean deleteMessage = true;
		for (String targetServer : targets.keySet()) {
			Collection<Recipient> temp = targets.get(targetServer);
			if (!deliver(targetServer, temp, message, mimemsg)) {
				// Retry this message later
				deleteMessage = false;
				if (!temp.isEmpty()) {
					undelivered.addAll(temp);
				}
			}
		}
		if (!deleteMessage) {
			try {
				message.setRetryCount(message.getRetryCount() + 1);
				message.setLastUpdate(new Date());
				recipients.clear();
				recipients.addAll(undelivered);
				message.store();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * We arranged that the recipients are all going to the same mail server. We
	 * will now rely on the DNS server to do DNS MX record lookup and try to
	 * deliver to the multiple mail servers. If it fails, we should decide that
	 * the failure is permanent or temporary.
	 * 
	 * @param host
	 *            the same host of recipients
	 * @param recipients
	 *            recipients who are all going to the same mail server
	 * @param message
	 *            Mail object to be delivered
	 * @param mimemsg
	 *            MIME message representation of the message
	 * @return true if the delivery was successful or permanent failure so the
	 *         message should be deleted, otherwise false and the message will
	 *         be tried to send again later
	 */
	private boolean deliver(String host, Collection<Recipient> recipients,
			SmtpMessage message, MimeMessage mimemsg) {
		// Prepare javamail recipients
		InternetAddress[] addresses = new InternetAddress[recipients.size()];
		Iterator<Recipient> it = recipients.iterator();
		for (int i = 0; it.hasNext(); i++) {
			Recipient rcpt = it.next();
			addresses[i] = rcpt.toInternetAddress();
		}
		
		try {
			// Lookup the possible targets
			Iterator<HostAddress> targetServers = null;
			if (null == gateway) {
				targetServers = getSmtpHostAddresses(host);
			} else {
				targetServers = getGatewaySmtpHostAddresses(gateway);
			}
			if (!targetServers.hasNext()) {
				logger.info("No mail server found for: " + host);
				StringBuilder exceptionBuffer = new StringBuilder(128).append(
						"There are no DNS entries for the hostname ").append(
						host).append(
						".  I cannot determine where to send this message.");
				return failMessage(message, addresses, new MessagingException(
						exceptionBuffer.toString()), false);
			}
			
			Properties props = session.getProperties();
			if (message.isNotificationMessage()) {
				props.put("mail.smtp.from", "<>");
			} else {
				props.put("mail.smtp.from", message.getFrom().getMailbox());
			}
		
			MessagingException lastError = null;
			StringBuilder logBuffer = null;
			HostAddress outgoingMailServer = null;
			while (targetServers.hasNext()) {
				try {
					outgoingMailServer = targetServers.next();
					logBuffer = new StringBuilder(256)
							.append("Attempting to deliver message to host ")
							.append(outgoingMailServer.getHostName())
							.append(" at ")
							.append(outgoingMailServer.getHost())
							.append(" for addresses ")
							.append(Arrays.asList(addresses));
					logger.info(logBuffer.toString());
					Transport transport = null;
					try {
						transport = session.getTransport(outgoingMailServer);
						try {
							if (authUser != null) {
								transport.connect(
										outgoingMailServer.getHostName(),
										authUser, authPass);
							} else {
								transport.connect();
							}
						} catch (MessagingException e) {
							// Any error on connect should cause the mailet to
							// attempt to connect to the next SMTP server
							// associated with this MX record.
							logger.error(e.getMessage());
							continue;
						}
						transport.sendMessage(mimemsg, addresses);
					} finally {
						if (transport != null) {
							try {
								transport.close();
							} catch (MessagingException e) {
							}
							transport = null;
						}
					}
					logBuffer = new StringBuilder(256)
							.append("Successfully sent message to host ")
							.append(outgoingMailServer.getHostName())
							.append(" at ")
							.append(outgoingMailServer.getHost())
							.append(" for addresses ")
							.append(Arrays.asList(addresses));
					logger.info(logBuffer.toString());
					recipients.clear();
					return true;
				} catch (SendFailedException sfe) {
					if (sfe.getValidSentAddresses() != null) {
						Address[] validSent = sfe.getValidSentAddresses();
						if (validSent.length > 0) {
							logBuffer = new StringBuilder(256)
									.append("Successfully sent message to host ")
									.append(outgoingMailServer.getHostName())
									.append(" at ")
									.append(outgoingMailServer.getHost())
									.append(" for addresses ")
									.append(Arrays.asList(validSent));
							logger.info(logBuffer.toString());
							// Remove the addresses to which this message was
							// sent successfully
							List<InternetAddress> temp = new ArrayList<InternetAddress>();
							for (int i = 0; i < addresses.length; i++) {
								if (!ArrayUtils.contains(validSent, addresses[i])) {
									if (addresses[i] != null) {
										temp.add(addresses[i]);
									}
								}
							}
							addresses = temp.toArray(new InternetAddress[temp.size()]);
							removeAll(recipients, validSent);
						}
					}
					
					if (sfe instanceof SMTPSendFailedException) {
						SMTPSendFailedException ssfe = (SMTPSendFailedException) sfe;
						// If permanent error 5xx, terminate this delivery
						// attempt by re-throwing the exception
						if (ssfe.getReturnCode() >= 500 && ssfe.getReturnCode() <= 599)
							throw sfe;
					}
					
					if (!ArrayUtils.isEmpty(sfe.getValidUnsentAddresses())) {
						// Valid addresses remained, so continue with any other server.
						if (logger.isDebugEnabled())
							logger
									.debug("Send failed, "
											+ sfe.getValidUnsentAddresses().length
											+ " valid recipients("
											+ Arrays.asList(sfe
													.getValidUnsentAddresses())
											+ ") remain, continuing with any other servers");
			            lastError = sfe;
						continue;
					} else {
						// There are no valid addresses left to send, so re-throw
						throw sfe;
					}
				} catch (MessagingException me) {
					Exception ne;
					if ((ne = me.getNextException()) != null
							&& ne instanceof IOException) {
						// It can be some socket or weird I/O related problem.
						lastError = me;
						continue;
					}
					throw me;
				}
			} // end while
			if (lastError != null) {
				throw lastError;
			}
		} catch (SendFailedException sfe) {
            boolean deleteMessage = false;
            
			if (sfe instanceof SMTPSendFailedException) {
				SMTPSendFailedException ssfe = (SMTPSendFailedException) sfe;
				deleteMessage = (ssfe.getReturnCode() >= 500 && ssfe.getReturnCode() <= 599);
			} else {
				// Sometimes we'll get a normal SendFailedException with nested
				// SMTPAddressFailedException, so use the latter RetCode
                MessagingException me = sfe;
                Exception ne;
                while ((ne = me.getNextException()) != null && ne instanceof MessagingException) {
                    me = (MessagingException)ne;
                    if (me instanceof SMTPAddressFailedException) {
                        SMTPAddressFailedException ssfe = (SMTPAddressFailedException)me;
                        deleteMessage = (ssfe.getReturnCode() >= 500 && ssfe.getReturnCode() <= 599);
                    }
                }
			}
			if (!ArrayUtils.isEmpty(sfe.getInvalidAddresses())) {
				// Invalid addresses should be considered permanent
				Address[] invalid = sfe.getInvalidAddresses();
				removeAll(recipients, invalid);
				deleteMessage = failMessage(message, invalid, sfe, true);
			}
			if (!ArrayUtils.isEmpty(sfe.getValidUnsentAddresses())) {
				// Vaild-unsent addresses should be considered temporary
				deleteMessage = failMessage(message, sfe.getValidUnsentAddresses(), sfe, false);
			}
			return deleteMessage;
		} catch (MessagingException mex) {
			// Check whether this is a permanent error (like account doesn't
			// exist or mailbox is full or domain is setup wrong)
			// We fail permanently if this was 5xx error.
			return failMessage(message, addresses, mex, ('5' == mex
					.getMessage().charAt(0)));
		}
		// If we get here, we've exhausted the loop of servers without sending
		// the message or throwing an exception.
		// One case where this might happen is if there is no server we can
		// connect. So this should be considered temporary
		return failMessage(message, addresses, new MessagingException(
				"No mail server(s) available at this time."), false);
	}
	
	/**
	 * Build error messages and make sure the error is permanent.
	 * 
	 * @param message
	 *            com.hs.mail.smtp.message.SmtpMessage
	 * @param addresses
	 *            addresses not delivered
	 * @param ex
	 *            javax.mail.MessagingException
	 * @param permanent
	 *            true if the exception is permanent error, false otherwise
	 * @return Whether the message failed fully and can be deleted
	 */
	private boolean failMessage(SmtpMessage message, Address[] addresses,
			MessagingException ex, boolean permanent) {
        StringBuffer logBuffer =
            new StringBuffer(64)
        		.append((permanent) ? "Permanent" : "Temporary")
                .append(" exception delivering mail (")
                .append(message.getName())
                .append("): ")
                .append(ex.getMessage().trim());
        logger.error(logBuffer.toString());
		if (!permanent) {
			int retries = message.getRetryCount();
			if (retries < maxRetries) {
				return false;
			}
		}
		if (message.isNotificationMessage()) {
			if (logger.isDebugEnabled())
				logger.debug(
					"Null reverse-path: no bounce will be generated for "
							+ message.getName());
			return true;
		}
		String errorMessage = buildErrorMessage(addresses, ex);
		message.appendErrorMessage(errorMessage);
		return true;
	}

	/**
	 * Build error message from the exception.
	 */
	private String buildErrorMessage(Address[] addresses, MessagingException ex) {
		StringBuilder errorBuffer = new StringBuilder();
		if (!ArrayUtils.isEmpty(addresses)) {
			for (int i = 0; i < addresses.length; i++) {
				errorBuffer.append(addresses[i].toString()).append("\r\n");
			}
		}
		Exception ne = ex.getNextException();
		if (ne == null) {
			errorBuffer.append(ex.getMessage().trim());
		} else if (ne instanceof SendFailedException) {
			errorBuffer.append("Remote server told me: "
					+ ne.getMessage().trim());
		} else if (ne instanceof UnknownHostException) {
			errorBuffer.append("Unknown host: " + ne.getMessage().trim());
		} else if (ne instanceof ConnectException) {
			// Already formatted as "Connection timed out: connect"
			errorBuffer.append(ne.getMessage().trim());
		} else if (ne instanceof SocketException) {
			errorBuffer.append("Socket exception: " + ne.getMessage().trim());
		} else {
			errorBuffer.append(ne.getMessage().trim());
		}
		errorBuffer.append("\r\n");
		return errorBuffer.toString();
	}

	private static void removeAll(Collection<Recipient> recipients,
			Address[] addresses) {
		for (Iterator<Recipient> it = recipients.iterator(); it.hasNext();) {
			Recipient rcpt = it.next();
			for (Address address : addresses) {
				if (rcpt.getMailbox().equals(
						((InternetAddress) address).getAddress())) {
					it.remove();
					break;
				}
			}
		}
	}

	/**
	 * Figure out which servers to try to send to.  
	 */
	private static Iterator<HostAddress> getSmtpHostAddresses(String domainName) {
		DnsServer dns = (DnsServer) ComponentManager.getBean("dns.server");
		return dns.getSmtpHostAddress(domainName);
	}
	
	private static Iterator<HostAddress> getGatewaySmtpHostAddresses(
			final String gateway) {
		return new Iterator<HostAddress>() {
			private Iterator<HostAddress> addresses = null;

			public boolean hasNext() {
				if (addresses == null) {
					String server = gateway;
					String port = "25";
					
					int idx = server.indexOf(':');
					if (idx > 0) {
						port = server.substring(idx + 1);
						server = server.substring(0, idx);
					}
					
					final String nextGateway = server;
					final String nextGatewayPort = port;
					try {
						final InetAddress[] ips = DnsServer.getAllByName(nextGateway);
						addresses = new Iterator<HostAddress>() {
							private InetAddress[] ipAddresses = ips;
							int i = 0;
							
							public boolean hasNext() {
								return i < ipAddresses.length;
							}

							public HostAddress next() {
								return new HostAddress(nextGateway, "smtp://" + (ipAddresses[i++]).getHostAddress() + ":" + nextGatewayPort);
							}

							public void remove() {
								throw new UnsupportedOperationException ("remove not supported by this iterator");
							}
						};
					} catch (UnknownHostException uhe) {
						logger.error("Unknown gateway host: "
								+ uhe.getMessage().trim());
						logger.error("This could be a DNS server error or configuration error.");
					}
				}
				return addresses != null && addresses.hasNext();
			}

			public HostAddress next() {
				return addresses != null ? addresses.next() : null;
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"remove not supported by this iterator");
			}
		};
	}
	
}
