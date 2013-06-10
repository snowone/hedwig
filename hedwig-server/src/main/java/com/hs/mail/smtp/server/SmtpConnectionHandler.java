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
package com.hs.mail.smtp.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

import com.hs.mail.container.config.Config;
import com.hs.mail.container.server.ConnectionHandler;
import com.hs.mail.container.server.socket.TcpSocketChannel;
import com.hs.mail.container.server.socket.TcpTransport;
import com.hs.mail.exception.LookupException;
import com.hs.mail.smtp.SmtpSession;
import com.hs.mail.smtp.processor.SmtpProcessor;
import com.hs.mail.smtp.processor.SmtpProcessorFactory;

/**
 * 
 * @author Won Chul Doh
 * @since May 3, 2010
 * 
 */
public class SmtpConnectionHandler implements ConnectionHandler {
	
	private boolean debug = false;

	private PrintStream out;	// debug output stream

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public synchronized void setDebugOut(PrintStream out) {
		this.out = out;
	}

	public void handleConnection(Socket soc) throws IOException {
		TcpTransport trans = new TcpTransport();
		trans.setChannel(new TcpSocketChannel(soc));
		SmtpSession session = new SmtpSession(trans);
		session.setDebug(debug);
		if (debug && out != null) {
			session.setDebugOut(out);
		}

		String greetings = new StringBuilder()
				.append("220 ")
				.append(Config.getHelloName())
				.append(" Service ready")
				.toString();
		session.writeResponse(greetings);

		while (!trans.isSessionEnded()) {
			String line = trans.readLine();
			if (line != null) {
				if (debug)
					session.getDebugOut().println(line);
				StringTokenizer st = new StringTokenizer(line);
				if (!st.hasMoreTokens()) {
					break;
				}
				String command = st.nextToken().trim();
				try {
					SmtpProcessor processor = SmtpProcessorFactory
							.createSmtpProcessor(command);
					processor.process(session, trans, st);
				} catch (LookupException e) {
					session.writeResponse("500 5.5.1 Unknown command \"" + command
							+ "\"");
				}
			}
		}
	}
	
}
