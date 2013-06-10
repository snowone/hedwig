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
package com.hs.mail.container.server.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * 
 * @author Won Chul Doh
 * @since Jul 29, 2010
 *
 */
public class TLSServerSocketFactory implements ServerSocketFactory {

	protected SSLServerSocketFactory factory;
	
	private boolean authenticateClient = false;

	public TLSServerSocketFactory(SSLContext context) {
		factory = context.getServerSocketFactory();
	}

	public void setAuthenticateClient(boolean b) {
		this.authenticateClient = b;
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		final ServerSocket serverSocket = factory.createServerSocket(port);
		initServerSocket(serverSocket);
		return serverSocket;
	}

	public ServerSocket createServerSocket(int port, int backLog)
			throws IOException {
		final ServerSocket serverSocket = factory.createServerSocket(port,
				backLog);
		initServerSocket(serverSocket);
		return serverSocket;
	}

	public ServerSocket createServerSocket(int port, int backLog,
			InetAddress bindAddress) throws IOException {
		final ServerSocket serverSocket = factory.createServerSocket(port,
				backLog, bindAddress);
		initServerSocket(serverSocket);
		return serverSocket;
	}

	private void initServerSocket(final ServerSocket serverSocket) {
		final SSLServerSocket socket = (SSLServerSocket) serverSocket;
		
		// Enable all available cipher suites when the socket is connected
		final String[] cipherSuites = socket.getSupportedCipherSuites();
		socket.setEnabledCipherSuites(cipherSuites);
		
		// Set client authentication if necessary
		socket.setNeedClientAuth(authenticateClient);
	}
	
}
