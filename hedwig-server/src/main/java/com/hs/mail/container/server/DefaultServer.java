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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.task.TaskExecutor;

import com.hs.mail.container.server.socket.ServerSocketFactory;
import com.hs.mail.container.server.socket.SocketConnection;

/**
 * 
 * @author Won Chul Doh
 * @since May 3, 2010
 * 
 */
public class DefaultServer implements Server {

	static Logger logger = Logger.getLogger(DefaultServer.class);

	/**
	 * The port on which this server will be made available.
	 */
	protected int port = -1;

	/**
	 * The host name of bindTo address.
	 */
	protected String bind = null;

	/**
	 * Network interface to which the service will bind. If not set, the server
	 * binds to all available interfaces.
	 */
	protected InetAddress bindTo = null;

    /**
     * The connection backlog.
     */
	protected int backlog = -1;

    /**
	 * The connection idle timeout. Used primarily to prevent server problems
	 * from hanging a connection.
	 */
	protected int connectionTimeout = 0;
	
	/**
	 * Indicates whether or not TCP_NODELAY is enabled.
	 */
	protected boolean tcpNoDelay = true;
  
	/**
	 * Factory creating server sockets.
	 */
	protected ServerSocketFactory serverSocketFactory = null;
	
    /**
     * The connection handler used by this service.
     */
	protected ConnectionHandler connectionHandler = null;
	
    private TaskExecutor taskExecutor = null;
	
    private MainSocketThread mainThread = null;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getBind() {
		return bind;
	}

	public void setBind(String bind) {
		this.bind = bind;
	}
	
	public InetAddress getBindTo() {
		return bindTo;
	}

	public void setBindTo(InetAddress bindTo) {
		this.bindTo = bindTo;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}
	
	public ServerSocketFactory getServerSocketFactory() {
		return serverSocketFactory;
	}

	public void setServerSocketFactory(ServerSocketFactory factory) {
		this.serverSocketFactory = factory;
	}

	public ConnectionHandler getConnectionHandler() {
		return connectionHandler;
	}

	public void setConnectionHandler(ConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
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

	public void configure() throws Exception {
		if (!StringUtils.isEmpty(bind) && !"*".equals(bind)) {
			bindTo = InetAddress.getByName(bind);
		}
	}

	public void start() {
		mainThread = new MainSocketThread();
		mainThread.start();

		StringBuilder logBuffer = new StringBuilder(64)
				.append(getServiceType())
				.append(" started on port ")
				.append(port);
		Logger.getLogger("console").info(logBuffer.toString());
	}
	
	public void stop() {
		mainThread.setCanContinue(false);
	}
	
	class MainSocketThread extends Thread {

		protected boolean canContinue = true;
		protected ServerSocket serverSocket = null;

		public MainSocketThread() {
		}
		
        public synchronized void setCanContinue(boolean canContinue) {
            this.canContinue = canContinue;
        }

		public void run() {
			open();
			
			if (null == this.serverSocket) {
				return;
			}
			
			while (this.canContinue) {
				Socket soc = null;
				
				try {
					soc = this.serverSocket.accept();
					soc.setTcpNoDelay(tcpNoDelay);
					soc.setSoTimeout(connectionTimeout);
					
					SocketConnection sc = new SocketConnection(connectionHandler, soc);
					
					try {
						taskExecutor.execute(sc);
					} catch (Exception e) {
                        if (logger.isDebugEnabled()) {
							logger.error("Cannot add work into thread pool: " + e.getMessage(), e);
						} else {
							logger.error("Cannot add work into thread pool: " + e.getMessage());
						}
                        
                        if (soc != null) {
                        	try {
                        		soc.close();
                        	} catch (Exception ce) {
                        	}
                        }
					}
				} catch (IOException e) {
					if (soc != null) {
						try {
							IOUtils.closeQuietly(soc.getInputStream ());
							IOUtils.closeQuietly(soc.getOutputStream());
							soc.close();
						} catch (IOException ae) {
						}
						soc = null;
					}
					
					if (logger.isDebugEnabled()) {
						logger.warn("IOException occurred: " + e.getMessage(), e);
					} else {
						logger.warn("IOException occurred: " + e.getMessage());
					}
					
					try {
						synchronized (this) {
							if (this.canContinue) {
								if (logger.isInfoEnabled()) {
									logger.info("Will reopen server socket.");
								}
								this.serverSocket.close();
								open();
							}
						}
					} catch (Exception oe) {
                        if (logger.isDebugEnabled()) {
                            logger.error("Cannot reopen server socket: " + oe.getMessage(), oe);
                        } else {
                            logger.error("Cannot reopen server socket: " + oe.getMessage());
                        }
						break;
					}
				}
			}
		}

		protected void open() {
			if (null != bindTo) {
				try {
					this.serverSocket = serverSocketFactory.createServerSocket(port, backlog, bindTo);
				} catch (IOException e) {
					try {
						if (backlog > 0) {
							this.serverSocket = serverSocketFactory.createServerSocket(port, backlog);
						} else {
							this.serverSocket = serverSocketFactory.createServerSocket(port);
						}
					} catch (IOException ae) {
						if (logger.isDebugEnabled()) {
							logger.error("Cannot open server socket (" + port + "," + backlog + "):" + e.getMessage(), e);
						} else {
							logger.error("Cannot open server socket (" + port + "," + backlog + "):" + e.getMessage());
						}
					}
				}
			} else {
				try {
					if (backlog > 0) {
						this.serverSocket = serverSocketFactory.createServerSocket(port, backlog);
					} else {
						this.serverSocket = serverSocketFactory.createServerSocket(port);
					}
				} catch (IOException e) {
					if (logger.isDebugEnabled()) {
						logger.error("Cannot open server socket (" + port + "," + backlog + "):" + e.getMessage(), e);
					} else {
						logger.error("Cannot open server socket (" + port + "," + backlog + "):" + e.getMessage());
					}
				}
			}
		}
	}

}
