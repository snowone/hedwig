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
package com.hs.mail.security.login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of the password encoder that returns the MD5 hash of any
 * plaintext password passed into the encoder. The specification is available
 * from RFC 1321.
 * 
 */
public class MD5PasswordEncoder implements PasswordEncoder {

    private static final String MAGIC = "$1$";

    private static final char A64[] = {
        '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 
        'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 
        'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
        'w', 'x', 'y', 'z'
    };
    
    public MD5PasswordEncoder() {
    }

	public static String encodePassword(char password[]) {
		long time = System.currentTimeMillis();
		return crypt(Integer.toString((int) (time & 0xffffffffL), 16), password);
	}

	public String encode(char[] password) {
		return encodePassword(password);
	}

	public boolean compare(String encoded, char[] plain) {
		String enc = encoded;
		if (!enc.startsWith(MAGIC))
			throw new IllegalArgumentException(
					"Invalid password format (no magic)");
		enc = enc.substring(3);
		int index = enc.indexOf('$');
		if (index == -1) {
			throw new IllegalArgumentException(
					"Invalid password format (no salt)");
		} else {
			String salt = enc.substring(0, index);
			enc = enc.substring(index + 1);
			return encoded.equals(crypt(salt, plain));
		}
	}

	public static String crypt(String salt, char password[]) {
		if (salt.length() > 8)
			salt = salt.substring(0, 8);
		byte slt[] = salt.getBytes();
		byte pwd[] = new byte[password.length];
		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = (byte) password[i];
			password[i] = '\0';
		}

		MessageDigest md1 = getMD5();
		md1.update(pwd);
		md1.update(MAGIC.getBytes());
		md1.update(slt);
		MessageDigest md2 = getMD5();
		md2.update(pwd);
		md2.update(slt);
		md2.update(pwd);
		byte digest[] = md2.digest();
		for (int i = pwd.length; i > 0; i -= 16) {
			md1.update(digest, 0, i <= 16 ? i : 16);
		}

		for (int i = pwd.length; i > 0; i >>>= 1) {
			if ((i & 1) != 0)
				md1.update((byte) 0);
			else
				md1.update(pwd[0]);
		}

		digest = md1.digest();
		for (int i = 0; i < 1000; i++) {
			md1.reset();
			if ((i & 1) != 0)
				md1.update(pwd);
			else
				md1.update(digest);
			if (i % 3 != 0)
				md1.update(slt);
			if (i % 7 != 0)
				md1.update(pwd);
			if ((i & 1) != 0)
				md1.update(digest);
			else
				md1.update(pwd);
			digest = md1.digest();
		}

		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = 0;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(MAGIC).append(salt).append('$');
		long l = ui(digest[0]) << 16 | ui(digest[6]) << 8 | ui(digest[12]);
		sb.append(a64(l, 4));
		l = ui(digest[1]) << 16 | ui(digest[7]) << 8 | ui(digest[13]);
		sb.append(a64(l, 4));
		l = ui(digest[2]) << 16 | ui(digest[8]) << 8 | ui(digest[14]);
		sb.append(a64(l, 4));
		l = ui(digest[3]) << 16 | ui(digest[9]) << 8 | ui(digest[15]);
		sb.append(a64(l, 4));
		l = ui(digest[4]) << 16 | ui(digest[10]) << 8 | ui(digest[5]);
		sb.append(a64(l, 4));
		l = ui(digest[11]);
		sb.append(a64(l, 2));

		return sb.toString();
	}

	private static MessageDigest getMD5() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 digest algorithm not available");
		}
	}

	private static String a64(long l, int size) {
		StringBuffer sb = new StringBuffer();
		char a64[] = A64;
		for (int i = 0; i < size; i++) {
			sb.append(a64[(int) (l & 63L)]);
			l >>>= 6;
		}
		return sb.toString();
	}

	private static int ui(byte b) {
		return b & 0xff;
	}

	public static void main(String args[]) {
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i] + ": "
					+ encodePassword(args[i].toCharArray()));
		}
	}
    
}
