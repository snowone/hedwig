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
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * Base class for custom LoginModule.
 * 
 * @author Won Chul Doh
 * @since Jul 18, 2007
 * 
 */
public abstract class BasicLoginModule implements LoginModule {

	/**
	 * The authentication status.
	 */
	protected boolean success;

	/**
	 * The commit status.
	 */
	protected boolean commitSuccess;

	/**
	 * The Subject to be authenticated.
	 */
	protected Subject subject;

	/**
	 * The Principals authenticated.
	 */
	protected Principal[] principals;

	/**
	 * A CallbackHandler for communicating with the end user (prompting for
	 * usernames and passwords, for example).
	 */
	protected CallbackHandler callbackHandler;

	/**
	 * State shared with other configured LoginModules.
	 */
	protected Map sharedState;

	/**
	 * Options specified in the login Configuration for this particular
	 * LoginModule.
	 */
	protected Map options;

	/**
	 * Class for password encoder
	 */
	protected Class encoder;
	
	protected boolean debug = false;

	private static final Class DEFAULT_ENCODER_CLASS = com.hs.mail.security.login.PlaintextPasswordEncoder.class;

	protected BasicLoginModule() {
		success = false;
		commitSuccess = false;
		principals = null;
		encoder = DEFAULT_ENCODER_CLASS;
	}

    /**
     * Overriding to allow for proper initialization.
     * 
     * Standard JAAS.
     */
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        String s = getOption("encoder", null);
        if (s != null) {
        	try {
        		encoder = Class.forName(s);
        	} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException(
						"Password encoder not found: " + s);
        	}
        }
        debug = "true".equals(getOption("debug", "false"));
	}

    /**
     * Overriding to allow for certificate-based login.
     * 
     * Standard JAAS.
     */
	public boolean login() throws LoginException {
		if (null == callbackHandler) {
			throw new LoginException("Error: no CallbackHandler available "
					+ "to gather authentication information from the user");
		}
		try {
			// Setup default callback handlers.
			Callback[] callbacks = getDefaultCallbacks();

			callbackHandler.handle(callbacks);

			principals = validate(callbacks);

			if (null == principals) {
				throw new FailedLoginException(
						"Authentication failed: Password does not match");
			} else {
				success = true;
			}
			return true;
		} catch (LoginException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new LoginException(ex.getMessage());
		}
	}

    /**
     * Standard JAAS override.
     */
	public boolean logout() throws LoginException {
		subject.getPrincipals().clear();
        success = false;
        commitSuccess = false;
		principals = null;
		return true;
	}

    /**
     * Standard JAAS override.
     */
	public boolean abort() throws LoginException {
		// Clean out state
        logout();
		return true;
	}

    /**
     * Overriding to complete login process.
     * 
     * Standard JAAS.
     */
	public boolean commit() throws LoginException {
		if (success) {
			if (subject.isReadOnly()) {
				throw new LoginException("Subject is Readonly");
			}
			Set<Principal> p = subject.getPrincipals();
			for (int i = 0; i < principals.length; i++)
				p.add(principals[i]);
			commitSuccess = true;
		}
		return commitSuccess;
	}
	
	protected boolean checkPassword(String encoded, char plain[])
			throws LoginException {
		try {
			PasswordEncoder e = (PasswordEncoder) encoder.newInstance();
			return e.compare(encoded, plain);
		} catch (Exception e) {
			throw new LoginException("Internal error");
		}
	}

	protected abstract Principal[] validate(Callback[] callbacks)
			throws LoginException;

	protected Callback[] getDefaultCallbacks() {
		// Setup default callback handlers.
		Callback[] callbacks = { new NameCallback("user name: "),
				new PasswordCallback("password: ", false) };
		return callbacks;
	}

	protected String getOption(String name, String defaultValue) {
		if (null == options) {
			return defaultValue;
		}
		String option = (String) options.get(name);
		return (null == option) ? defaultValue : option;
	}

}
