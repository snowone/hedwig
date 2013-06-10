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
package com.hs.mail.container.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Factory used to create SSLContexts.
 * 
 * @author Won Chul Doh
 * @since Jul 29, 2010
 * 
 */
public class SSLContextFactory {

	/**
	 * Creates a SSLContext appropriate for use
	 * 
	 * @param keyStore
	 *            the Java keystore file
	 * @param keyStorePassword
	 *            password for the Java keystore
	 * @param certificatePassword
	 *            certificate password
	 * @return SSLContext
	 */
	public static SSLContext createContext(String keyStore,
			String keyStorePassword, String certificatePassword) {
		try {
			String algorithm = "SunX509";
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(asInputStream(keyStore), keyStorePassword.toCharArray());

			// Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, certificatePassword.toCharArray());

            // Initialize the SSLContext to work with our key managers.
            SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), null, null);

			return context;
		} catch (Exception e) {
			throw new Error(
                    "Failed to initialize the server-side SSLContext", e);
		}
	}

	private static InputStream asInputStream(String keyStore)
			throws FileNotFoundException {
		return new FileInputStream(new File(keyStore));
	}

}
