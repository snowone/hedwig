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
package com.hs.mail.security;

/**
 * Represents a user as a JAAS principal.
 * 
 * @author Won Chul Doh
 * @since Aug 5, 2010
 *
 */
public class UserPrincipal extends BasicPrincipal {

	private long id;
	
	public UserPrincipal(String name) {
		super(name);
	}

	public UserPrincipal(String name, long id) {
		super(name);
		this.id = id;
	}

	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

}
