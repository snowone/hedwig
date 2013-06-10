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
package com.hs.mail.smtp.message;

import javax.mail.URLName;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 10, 2010
 *
 */
public class HostAddress extends URLName {

	private String hostname;
	
	public HostAddress(String hostname, String url) {
		super(url);
		this.hostname = hostname;
	}

	public String getHostName() {
		return hostname;
	}
	
}
