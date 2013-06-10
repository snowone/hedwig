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
package com.hs.mail.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.ParseException;

import org.apache.commons.lang.StringUtils;

import com.hs.mail.imap.user.User;
import com.hs.mail.imap.user.UserManager;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 2, 2010 
 *
 */
public class DataImporter {

	List<ImportError> errors = new ArrayList<ImportError>();
	
	public void importAccount(UserManager manager, InputStream is)
			throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				User user = parseAccount(line);
				manager.addUser(user);
			} catch (Exception e) {
				addError(reader.getLineNumber(), line, e);
			}
		}
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public void addError(int number, String line, Exception e) {
		errors.add(new ImportError(number, line, e.getMessage()));
	}
	
	public List<ImportError> getErrors() {
		return errors;
	}
	
	private User parseAccount(String str) throws ParseException {
		String[] astr = StringUtils.split(str, ':');
		int len = astr.length;
		User user = new User();
		MailUtils.validateAddress(astr[0]);
		user.setUserID(astr[0]);
		user.setPassword("1"); // default password
		if (len > 1) {
			if (StringUtils.isNotEmpty(astr[1])) {
				user.setPassword(astr[1]);
			}
			if (len > 2) {
				if (StringUtils.isNotEmpty(astr[2])) {
					user.setQuota(Long.parseLong(astr[2]));
				}
				if (len > 3) {
					if (StringUtils.isNotEmpty(astr[3])) {
						MailUtils.validateAddress(astr[3]);
						user.setForwardTo(astr[3]);
					}
				}
			}
		}
		return user;
	}
	
	public class ImportError extends Exception {

		private static final long serialVersionUID = 1L;

		private String line;
		
		private int number;

		public ImportError(int number, String line, String message) {
			super(message);
			this.number = number;
			this.line = line;
		}

		public String getSourceLine() {
			return line;
		}
		
		public int getLineNumber() {
			return number;
		}

	}
	
}
