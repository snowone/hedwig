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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

import com.hs.mail.security.UserPrincipal;

/**
 * A LoginModule that allows for authentication based on Legacy database.
 * 
 * @author Won Chul Doh
 * @since Jul 18, 2007
 * 
 */
public class JdbcLoginModule extends BasicLoginModule {

    private String driver;
    private String url;
    private String username;
    private String password;
    private String query;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		super.initialize(subject, callbackHandler, sharedState, options);
		driver = getOption("driver", null);
		if (driver == null)
			throw new Error("No JDBC driver specified");
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JDBC driver not found: " + driver);
		}
		url = getOption("url", null);
		if (url == null) {
			throw new Error("No database URL specified");
		}
		username = getOption("username", null);
		password = getOption("password", null);
		if (username == null && password != null || username != null
				&& password == null) {
			throw new Error(
					"Both username and password option must be specified");
		}
		query = getOption("query", null);
		if (query == null) {
			throw new Error("Query not specified");
		}
	}
	
	@Override
	protected Principal[] validate(Callback[] callbacks) throws LoginException {
		String username = ((NameCallback) callbacks[0]).getName();
		char[] password = ((PasswordCallback) callbacks[1]).getPassword();

		Connection cn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {
			cn = (this.password != null) 
					? DriverManager.getConnection(url, this.username, this.password) 
					: DriverManager.getConnection(url);
			pstmt = cn.prepareStatement(query);
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();
			if (!rs.next())
				throw new AccountNotFoundException("Account for " + username
						+ " not found");
			String encodedPwd = rs.getString(1);
			Principal[] principals = new Principal[1];
			principals[0] = new UserPrincipal(username);
			boolean ok = checkPassword(encodedPwd, password);
			if (!ok)
				throw new CredentialException("Incorrect password for "
						+ username);
			else
				return principals;
		} catch (SQLException e) {
			throw new LoginException("Error while reading database: "
					+ e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (rs != null)
					rs.close();
				if (cn != null)
					cn.close();
			} catch (SQLException e) { }
		}
	}

}
