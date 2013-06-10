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
package com.hs.mail.sieve;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jsieve.ConfigurationManager;
import org.apache.jsieve.SieveConfigurationException;
import org.apache.jsieve.SieveFactory;
import org.apache.log4j.Logger;

import com.hs.mail.container.config.Config;
import com.hs.mail.mailet.MailetContext;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

public class Sieve {

	static Logger logger = Logger.getLogger(Sieve.class);
			
	public static final String DEFAULT_SIEVE = "default.sieve";

	private static SieveFactory  factory = null;
	static {
		try {
			ConfigurationManager config = new ConfigurationManager();
			factory = config.build();
		} catch (SieveConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static boolean runSieve(MailetContext context, Recipient recipient,
			SmtpMessage msg) {
		File script = getScript(context, recipient);
		if (script != null) {
			InputStream is = null;
			try {
				SieveMailAdapter adapter = new SieveMailAdapter(context,
						recipient.getMailbox(), recipient.getID());
				adapter.setMessage(msg);
				is = new BufferedInputStream(new FileInputStream(script));
				factory.interpret(adapter, is);
				return true;
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return false;
	}
	
	private static File getScript(MailetContext context, Recipient recipient) {
		File dir = context.getUserManager().getUserHome(recipient);
		File script = new File(dir, DEFAULT_SIEVE);
		if (script.exists()) {
			return script;
		}
		// If user's sieve script does not exist, then check for domain's
		// default sieve script.
		String domain = (recipient.getHost() != null) ? recipient.getHost()
				: Config.getDefaultDomain();
		dir = new File(Config.getDataDirectory(), domain);
		script = new File(dir, DEFAULT_SIEVE);
		return (script.exists()) ? script : null;
	}
	
}
