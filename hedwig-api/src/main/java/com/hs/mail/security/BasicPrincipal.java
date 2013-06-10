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

import java.security.Principal;

/**
 * A basic implementation of the <code>Principal</code> interface.
 * 
 * @author Won Chul Doh
 * @since Jul 18, 2007
 * 
 */
public class BasicPrincipal implements Principal {
	
	private String name;
	
	/**
	 * Initializes the principal with the given name
	 * 
	 * @param name
	 *            The given name
	 */
	public BasicPrincipal(String name) {
		if (name == null) {
			throw new NullPointerException("Null name");
		}
		this.name = name.trim();
		if (name.length() == 0) {
			throw new IllegalArgumentException("Invalid name");
		}
	}

	/**
	 * Retrieves the name of the principal
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else
			return getClass().isInstance(obj)
					&& name.equals(((Principal) obj).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + "\"" + name + "\"";
	}
	
}
