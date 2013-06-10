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
package com.hs.mail.security.login;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.StringTokenizer;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;

import com.hs.mail.io.LineReader;
import com.hs.mail.security.RolePrincipal;
import com.hs.mail.security.UserPrincipal;

/**
 * A LoginModule that allows for authentication based on properties file.
 * 
 * @author Won Chul Doh
 * @since Aug 5, 2010
 *
 */
public class PropertiesLoginModule extends BasicLoginModule {

	private File file;
	
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		super.initialize(subject, callbackHandler, sharedState, options);
		
		File baseDir = null;
		if (System.getProperty("java.security.auth.login.config") != null) {
			baseDir = new File(System.getProperty("java.security.auth.login.config")).getParentFile();
		} else {
			baseDir = new File(".");
		}

		String filename = getOption("file", null);
		if (filename == null)
			throw new RuntimeException("No file specified");
		file = new File(baseDir, filename);
	}

	@Override
	protected Principal[] validate(Callback[] callbacks) throws LoginException {
		String username = ((NameCallback) callbacks[0]).getName();
		char[] password = ((PasswordCallback) callbacks[1]).getPassword();

		String entry = getLine(file, username + "=");
		if (entry == null)
			throw new AccountNotFoundException("Account for " + username
					+ " not found");
		int index = entry.indexOf('=');
		if (index == -1)
			throw new FailedLoginException("Invalid user record");
		entry = entry.substring(index + 1);
		index = entry.indexOf(':');
		if (index == -1)
			throw new FailedLoginException("Invalid user record");
		String encodedPwd = entry.substring(0, index);
		String roles = entry.substring(index + 1);
		StringTokenizer st = new StringTokenizer(roles, ",");
		Principal[] principals = new Principal[st.countTokens() + 1];
		for (int i = 0; i < principals.length -1; i++) {
			principals[i] = new RolePrincipal(st.nextToken().trim());
		}
		principals[principals.length - 1] = new UserPrincipal(username);
		boolean ok = checkPassword(encodedPwd, password);
		if (!ok)
			throw new CredentialException("Incorrect password for " + username);
		else
			return principals;
	}

	private String getLine(File file, String start) throws LoginException {
		LineReader reader = null;
		BufferedInputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			reader = new LineReader(is);
			int len = start.length();
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("#")
						&& line.regionMatches(false, 0, start, 0, len)) {
					return line;
				}
			}
			return null;
		} catch (IOException e) {
			throw new LoginException("Error while reading file: " + file);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}
	
}
