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
package com.hs.mail.smtp.processor;

import java.util.Hashtable;
import java.util.Map;

import com.hs.mail.exception.LookupException;

/**
 * 
 * @author Won Chul Doh
 * @since May 29, 2010
 * 
 */
public class SmtpProcessorFactory {
	
	public static SmtpProcessor createSmtpProcessor(String command) {
		SmtpProcessor processor = lookup(command);
		return processor;
	}

	private static SmtpProcessor lookup(String command) {
		SmtpProcessor processor = processorMap.get(command.toLowerCase());
		if (null == processor)
			throw new LookupException("Class for '" + command + "' not found.");
		return processor;
	}

	static private Map<String, SmtpProcessor> processorMap = new Hashtable<String, SmtpProcessor>();
	static {
		processorMap
				.put("auth", new com.hs.mail.smtp.processor.AuthProcessor());
		processorMap
				.put("data", new com.hs.mail.smtp.processor.DataProcessor());
		processorMap
				.put("ehlo", new com.hs.mail.smtp.processor.EhloProcessor());
		processorMap
				.put("expn", new com.hs.mail.smtp.processor.ExpnProcessor());
		processorMap
				.put("helo", new com.hs.mail.smtp.processor.HeloProcessor());
		processorMap
				.put("help", new com.hs.mail.smtp.processor.HelpProcessor());
		processorMap
				.put("mail", new com.hs.mail.smtp.processor.MailProcessor());
		processorMap
				.put("noop", new com.hs.mail.smtp.processor.NoopProcessor());
		processorMap
				.put("quit", new com.hs.mail.smtp.processor.QuitProcessor());
		processorMap
				.put("rcpt", new com.hs.mail.smtp.processor.RcptProcessor());
		processorMap
				.put("rset", new com.hs.mail.smtp.processor.RsetProcessor());
		processorMap
				.put("vrfy", new com.hs.mail.smtp.processor.VrfyProcessor());
	}

}
