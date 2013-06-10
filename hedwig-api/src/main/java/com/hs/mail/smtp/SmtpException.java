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
package com.hs.mail.smtp;

/**
 * 
 * @author Won Chul Doh
 * @since May 30, 2010
 *
 */
public class SmtpException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public static final String RECIPIENTS_COUNT_LIMIT = "452 4.5.3 Maximum recipients reached";

	public static final String DOMAIN_NAME_LENGTH_LIMIT = "501 5.5.0 Domain name length exceeds fixed limit";

	public static final String CANNOT_DECODE_PARAM = "501 5.5.4 Could not decode parameters";
	
	public static final String INVALID_SIZE_PARAM = "501 5.5.4 Syntactically incorrect value for SIZE parameter";

	public static final String INVALID_COMMAND_PARAM = "501 5.5.4 Invalid command parameters";

	public static final String MISSING_DOMAIN_ADDRESS = "501 5.5.4 Domain address required";

	public static final String MISSING_RECIPIENT_ADDRESS = "501 5.5.4 Missing recipient address";

	public static final String MISSING_SENDER_ADDRESS = "501 5.5.4 Missing sender address";

	public static final String EXPN_NOT_SUPPORTED = "502 5.3.3 EXPN is not supported";

	public static final String VRFY_NOT_SUPPORTED = "502 5.3.3 VRFY is not supported";
	
	public static final String ALREADY_AUTHENTICATED = "503 5.5.0 User already authenticated";

	public static final String MESSAGE_SIZE_LIMIT = "552 5.3.4 Message size exceeds fixed maximum message size";

	public static final String COMMAND_OUT_OF_SEQUENCE = "503 5.5.1 Command out of sequence";
	
	public static final String AUTHTYPE_NOT_SUPPORTED = "504 5.5.1 Unsupported authentication type";
	
	public static final String AUTH_REQUIRED = "530 5.7.1 Authentication required";
	
	public static final String AUTH_FAILED = "535 5.7.0 Authentication failed";

	public static final String RELAY_DENIED = "550 5.7.1 Relaying denied";

	public static final String NO_VALID_RECIPIENTS = "554 5.5.0 No valid recipients";

	/**
	 * Constructor for SmtpException.
	 */
	public SmtpException() {
		super();
	}

	/**
	 * Constructor for SmtpException.
	 * 
	 * @param message
	 */
	public SmtpException(String message) {
		super(message);
	}

	/**
	 * Constructor for SmtpException.
	 * 
	 * @param message
	 * @param cause
	 */
	public SmtpException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for SmtpException.
	 * 
	 * @param cause
	 */
	public SmtpException(Throwable cause) {
		super(cause);
	}

	
}
