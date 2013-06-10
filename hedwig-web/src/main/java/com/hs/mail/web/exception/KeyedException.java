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
package com.hs.mail.web.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 1, 2010
 *
 */
public class KeyedException extends Exception {

	private static final long serialVersionUID = -5746188817229405014L;

	private String key;
	
	public KeyedException(String key) {
		this.key = key;
	}

	public KeyedException(String key, Throwable cause) {
		super(cause);
		this.key = key;
	}	
	
	public String getKey() {
		return key;
	}
	
	public String getExceptionTrace() {
		StringWriter trace = new StringWriter();
		printStackTrace(new PrintWriter(trace));
		return trace.toString();
	}
	
}
