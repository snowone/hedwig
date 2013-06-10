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

import java.io.IOException;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginException;

import com.hs.mail.container.server.socket.TcpTransport;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.smtp.SmtpException;
import com.hs.mail.smtp.SmtpSession;
import com.hs.mail.util.BASE64;

/**
 * Handler for AUTH command.
 * 
 * @author Won Chul Doh
 * @since Jun 27, 2010
 *
 */
public class AuthProcessor extends AbstractSmtpProcessor {

	private static final String AUTH_TYPE_PLAIN = "PLAIN";
	
	private static final String AUTH_TYPE_LOGIN = "LOGIN";
	
	@Override
	protected void doProcess(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws SmtpException {
		try {
			doAuth(session, trans, st);
		} catch (SmtpException e) {
			// re-throw this exception
			throw e;
		} catch (Exception e) {
			// Instead of throwing exception just end the session
			trans.endSession();
		}
	}

	/**
	 * Handles client authentication to the SMTP server. 
	 */
	private void doAuth(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws Exception {
		if (session.getAuthID() > 0) {
			throw new SmtpException(SmtpException.ALREADY_AUTHENTICATED);
		} else if (st.countTokens() < 1) {
			throw new SmtpException(SmtpException.INVALID_COMMAND_PARAM);
		}
		String authType = nextToken(st);
		if (AUTH_TYPE_PLAIN.equalsIgnoreCase(authType)) {
			doPlainAuth(session, trans, st);
		} else if (AUTH_TYPE_LOGIN.equalsIgnoreCase(authType)) {
			doLoginAuth(session, trans, st);
		} else {
			throw new SmtpException(SmtpException.AUTHTYPE_NOT_SUPPORTED + ": "
					+ authType);
		}
	}

	/**
	 * Handle the Plain AUTH SASL exchange. According to RFC 2595 the client
	 * must send: [authorize-id] \0 authenticate-id \0 password.
	 */
	private void doPlainAuth(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws IOException {
		String userpass = null, user = null, pass = null;
		if (st.hasMoreTokens()) {
			userpass = nextToken(st);
		} else {
			session.writeResponse("334 OK. Continue authentication");
			userpass = trans.readLine();
		}
		try {
			userpass = new String(BASE64.decode(userpass));
			StringTokenizer args = new StringTokenizer(userpass, "\0");
			if (args.countTokens() == 3) {
				// RFC 2595 says that "the client may leave the authorization
				// identity empty to indicate that it is the same as the
				// authentication identity.
				// So skip the authorization identity
				args.nextToken();
			}
			user = args.nextToken();	// Authentication identity
			pass = args.nextToken();	// Password
		} catch (Exception e) {
			// Ignore - this exception will be dealt with in the if clause below
		}
		// Authenticate user
		authenticate(session, trans, user, pass);
	}

	/**
	 * Handle the Login AUTH SASL exchange.
	 */
	private void doLoginAuth(SmtpSession session, TcpTransport trans,
			StringTokenizer st) throws IOException {
		String user = null, pass = null;
		if (st.hasMoreTokens()) {
			user = nextToken(st);
		} else {
			session.writeResponse("334 VXNlcm5hbWU6");	// base64 encoded "Username:"
			user = trans.readLine();
		}
		try {
			user = new String(BASE64.decode(user));
		} catch (Exception e) {
			// Ignore - this exception will be dealt with in the if clause below
			user = null;
		}
		session.writeResponse("334 UGFzc3dvcmQ6"); // base64 encoded "Password:"
		pass = trans.readLine();
		try {
			pass = new String(BASE64.decode(pass));
		} catch (Exception e) {
			// Ignore - this exception will be dealt with in the if clause below
			pass = null;
		}
		// Authenticate user
		authenticate(session, trans, user, pass);
	}
	
	private void authenticate(SmtpSession session, TcpTransport trans,
			String user, String pass) {
		if ((user == null) || (pass == null)) {
			throw new SmtpException(SmtpException.CANNOT_DECODE_PARAM);
		} else {
			try {
				UserManager manager = getUserManager();
				long authID = manager.login(user, pass);
				session.setAuthID(authID);
				session.writeResponse("235 2.7.0 Authentication Successful");
			} catch (LoginException e) {
				throw new SmtpException(SmtpException.AUTH_FAILED);
			}
		}
	}
	
}
