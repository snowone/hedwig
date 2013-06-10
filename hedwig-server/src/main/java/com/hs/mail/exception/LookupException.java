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
package com.hs.mail.exception;

public class LookupException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for LookupException.
	 */
	public LookupException() {
		super();
	}

	/**
	 * Constructor for LookupException.
	 * 
	 * @param message
	 */
	public LookupException(String message) {
		super(message);
	}

	/**
	 * Constructor for LookupException.
	 * 
	 * @param message
	 * @param cause
	 */
	public LookupException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for LookupException.
	 * 
	 * @param cause
	 */
	public LookupException(Throwable cause) {
		super(cause);
	}

}
