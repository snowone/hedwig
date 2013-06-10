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

/**
 * Interface to provide a standard way to translate a plaintext password into a
 * different representation of that password so that the password may be
 * compared with the stored encrypted password without having to decode the
 * encrypted password.
 * <p>
 * PasswordEncoder are useful because often the stored passwords are encoded
 * with a one way hash function which makes them almost impossible to decode.
 * 
 * @author Won Chul Doh
 * @since Jul 18, 2007
 * 
 */
public interface PasswordEncoder {

    public String encode(char password[]);

    public boolean compare(String encoded, char plain[]);

}
