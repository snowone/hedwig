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

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;

import com.hs.mail.security.UserPrincipal;

/**
 * A LoginModule that allows for authentication based on LDAP directory.
 * 
 * @author Won Chul Doh
 * @since Aug 7, 2010
 * 
 */
public class JndiLoginModule extends BasicLoginModule {

	private String contextFactory;
	private String url;
	private String username; 
	private String password;
	private String authentication;
	private String base;
	private String searchFilter;
	private String returnAttribute;
	private boolean subtree;
	private MessageFormat searchFilterFormat; 
    protected DirContext context = null;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		super.initialize(subject, callbackHandler, sharedState, options);
		contextFactory = getOption("context.factory", "com.sun.jndi.ldap.LdapCtxFactory");
		url = getOption("url", null);
		if (url == null) {
			throw new Error("No JNDI URL specified");
		}
		username = getOption("username", null);
		password = getOption("password", null);
		authentication = getOption("authentication", "simple");
		base = getOption("base", null);
		String filter = getOption("searchFilter", "(uid={0})");
		searchFilterFormat = new MessageFormat(filter);
		returnAttribute = getOption("returnAttribute", null);
		subtree = new Boolean(getOption("subtree", "true")).booleanValue();
	}
	
	@Override
	protected Principal[] validate(Callback[] callbacks) throws LoginException {
		String username = ((NameCallback) callbacks[0]).getName();
		char[] password = ((PasswordCallback) callbacks[1]).getPassword();

		Principal[] principals = new Principal[1];
		principals[0] = new UserPrincipal(username);
		try {
			boolean ok = authenticate(username, String.valueOf(password));
			if (!ok)
				throw new CredentialException("Incorrect password for "
						+ username);
			else
				return principals;
		} catch (Exception e) {
			throw (LoginException) new LoginException("LDAP Error")
					.initCause(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean authenticate(String username, String password)
			throws Exception {
		DirContext context = null;
		try {
			context = open();
			searchFilterFormat.format(new String[] { username });
			SearchControls constraints = new SearchControls(); 
			constraints.setSearchScope(subtree ? SearchControls.SUBTREE_SCOPE
					: SearchControls.ONELEVEL_SCOPE);
			if (returnAttribute != null) {
				String[] attribs = StringUtils.split(returnAttribute, ",");
				constraints.setReturningAttributes(attribs);
			}
			NamingEnumeration ne = context.search(base, searchFilter,
					constraints);
			if (ne == null || !ne.hasMore()) {
				return false;
			}
			SearchResult sr = (SearchResult) ne.next();
			if (ne.hasMore()) {
				// Ignore for now
			}
			// Check the credentials by binding to server
			if (bindUser(context, sr.getNameInNamespace(), password)) {
				return true;
			} else {
				return true;
			}
		} catch (NamingException e) {
			close(context);
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected DirContext open() throws NamingException {
		if (context == null) {
			try {
				// Set up the environment for creating the initial context
				Hashtable env = new Hashtable();
				env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
				if (StringUtils.isNotEmpty(username)) {
					env.put(Context.SECURITY_PRINCIPAL, username);
				}
				if (StringUtils.isNotEmpty(password)) {
					env.put(Context.SECURITY_CREDENTIALS, password);
				}
				env.put(Context.PROVIDER_URL, url);
				env.put(Context.SECURITY_AUTHENTICATION, authentication);
				context = new InitialDirContext(env);
			} catch (NamingException e) {
				throw e;
			}
		}
		return context;
	}
	
	private boolean bindUser(DirContext context, String dn, String password)
			throws NamingException {
		boolean isValid = false;
		context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
		context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
		try {
			context.getAttributes("", null);
			isValid = true;
		} catch (AuthenticationException e) {
		}
		if (StringUtils.isNotEmpty(this.username)) {
			context.addToEnvironment(Context.SECURITY_PRINCIPAL, this.username);
		} else {
			context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
		}
		if (StringUtils.isNotEmpty(this.password)) {
			context.addToEnvironment(Context.SECURITY_CREDENTIALS,
					this.password);
		} else {
			context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
		}
		return isValid;
	}

	protected void close(DirContext context) {
		if (context != null) {
			try {
				context.close();
				context = null;
			} catch (Exception e) {
			}
		}
	}
	
}
