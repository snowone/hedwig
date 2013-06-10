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
package com.hs.mail.web;

import java.io.Serializable;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hs.mail.web.util.RequestUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 1, 2010
 *
 */
public class WebSession implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String LOGIN_CONTEXT = "lc";

	public static final String ACCOUNT_COUNT = "/account.count";
	
	public static final String ALIAS_COUNT = "/alias.count";
	
	// http session state related
	transient private HttpServletRequest request;
	
	transient private HttpServletResponse response;
	
	transient private HttpSession websession;
	
	public WebSession(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.websession = request.getSession(true);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public String getReturnUrl() {
		return RequestUtils.getReturnUrl(request, response);
	}
	
	public boolean isValid() {
		return retrieveBean(LOGIN_CONTEXT) != null;
	}
	
	public void storeBean(String name, Object bean) {
		websession.setAttribute(name, bean);
	}
	
	public Object retrieveBean(String name) {
		return websession.getAttribute(name);
	}
	
	public void removeBean(String name) {
		websession.removeAttribute(name);
	}
	
	public void removeBeans(String postfix) {
		Enumeration enums = websession.getAttributeNames();
		while (enums.hasMoreElements()) {
			String name = (String) enums.nextElement();
			if (name.endsWith(postfix)) {
				websession.removeAttribute(name);
			}
		}
	}

}
