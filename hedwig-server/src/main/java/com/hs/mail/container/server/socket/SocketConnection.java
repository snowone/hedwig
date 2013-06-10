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
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.hs.mail.container.server.ConnectionHandler;


/**
 * 
 * @author Won Chul Doh
 * @since May 3, 2010
 * 
 */
public class SocketConnection implements Runnable {

    static Logger logger = Logger.getLogger(SocketConnection.class);
    
    private ConnectionHandler handler = null;
    private Socket socket = null;
	
    public SocketConnection(ConnectionHandler handler, Socket socket) {
        this.handler = handler;
        this.socket = socket;
    }

    public void run() {
        try {
            handler.handleConnection(socket);
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        } finally {
        	if (socket != null) {
        		try {
        			IOUtils.closeQuietly(socket.getInputStream ());
        			IOUtils.closeQuietly(socket.getOutputStream());
					socket.close();
				} catch (IOException e) {
				}
        		socket = null;
        	}
        }

	}

}
