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
package com.hs.mail.imap.server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.springframework.beans.factory.InitializingBean;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.server.codec.ImapMessageEncoder;
import com.hs.mail.imap.server.codec.ImapRequestDecoder;

/**
 * NIO IMAP Server which use Netty
 * 
 * @author Won Chul Doh
 * @since Jan 12, 2010
 */
public class ImapServer implements InitializingBean {
	
	private static Logger log = Logger.getLogger(ImapServer.class);

	/**
	 * The host name of network interface to which the service will bind. If not
	 * set, the server binds to all available interfaces.
	 */
	private String bind = null;

	/**
	 * The port on which this server will be made available.
	 */
	private int port = 143;
	
	/**
	 * Whether TLS is enabled for this server's socket?
	 */
	private boolean useTLS = false;
	
	private ReadTimeoutHandler timeoutHandler;
	private ImapServerHandler handler;

	public String getBind() {
		return bind;
	}

	public void setBind(String bind) {
		this.bind = bind;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setUseTLS(boolean useTLS) {
		this.useTLS = useTLS;
	}

	public boolean isUseTLS() {
		return useTLS;
	}
	
	/**
     * This method returns the type of service provided by this server.
     * This should be invariant over the life of the class.
     *
     * Subclasses may override this implementation.  This implementation
     * parses the complete class name and returns the undecorated class
     * name.
     *
     * @return description of this server
     */
	public String getServiceType() {
		String name = getClass().getName();
		int i = name.lastIndexOf(".");
		if (i > 0 && i < name.length() - 2) {
			name = name.substring(i + 1);
		}
		return name;
	}

	public void afterPropertiesSet() throws Exception {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(Executors
						.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Do not create many instances of Timer.
		// HashedWheelTimer creates a new thread whenever it is instantiated and
		// started. Therefore, you should make sure to create only one instance
		// and share it across your application.
		Timer timer = new HashedWheelTimer();
		// Set 30-minute read timeout.
		int timeout = (int) Config.getNumberProperty("imap_timeout", 1800);
		// timer must be shared.
		timeoutHandler = new ReadTimeoutHandler(timer, timeout);
		// prepare business logic handler
		handler = new ImapServerHandler();
		
		bootstrap.setPipelineFactory(createPipelineFactory());

		// Bind and start to accept incoming connections.
		InetSocketAddress endpoint = null;
		if (!StringUtils.isEmpty(bind) && !"*".equals(bind)) {
			InetAddress bindTo = InetAddress.getByName(bind);
			endpoint = new InetSocketAddress(bindTo, getPort());
		} else {
			endpoint = new InetSocketAddress(getPort());
		}
		bootstrap.bind(endpoint);
		
		StringBuilder logBuffer = new StringBuilder(64)
				.append(getServiceType())
				.append(" started on port ")
				.append(getPort());
		Logger.getLogger("console").info(logBuffer.toString());
	}
	
	private ChannelPipelineFactory createPipelineFactory() {

		return new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = Channels.pipeline();

				if (isUseTLS()) {
					SSLEngine engine = Config.getSSLContext().createSSLEngine();
					engine.setUseClientMode(false);
					pipeline.addFirst("ssl", new SslHandler(engine));
				}
				if (Config.getBooleanProperty("imap_trace_protocol", false)) {
					pipeline.addLast("debug", createDebuggingHandler());
				}
				pipeline.addLast("timeout", timeoutHandler);
				int maxLineLength = (int) Config.getNumberProperty("imap_line_limit", 8192);
				pipeline.addLast("decoder", new ImapRequestDecoder(maxLineLength));
				pipeline.addLast("encoder", new ImapMessageEncoder());

				// and then business logic.
				pipeline.addLast("handler", handler);

				return pipeline;
			}
			
		};
	}
	
	private DebuggingHandler createDebuggingHandler() {
		DebuggingHandler handler = new DebuggingHandler();
		String path = Config.getProperty("imap_protocol_log", null);
		if (path != null) {
			try {
				FileOutputStream fos = new FileOutputStream(path);
				handler.setDebugOut(new PrintStream(fos));
			} catch (FileNotFoundException e) {
				// Ignore this exception
				log.error(e.getMessage());
			}
		}
		return handler;
	}
	
}
