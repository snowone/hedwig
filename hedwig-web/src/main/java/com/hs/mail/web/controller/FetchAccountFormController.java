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
package com.hs.mail.web.controller;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.hs.mail.web.fetchmail.FetchAccount;
import com.hs.mail.web.fetchmail.FetchMailer;
import com.hs.mail.web.util.RequestUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 2, 2010
 *
 */
public class FetchAccountFormController extends SimpleFormController {

	@Override
	protected boolean isFormSubmission(HttpServletRequest request) {
		String todo = request.getParameter("todo");
		return "docreate".equals(todo) || "doupdate".equals(todo);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		long userID = RequestUtils.getParameterLong(request, "ID", 0);
		FetchAccount fetch = new FetchAccount();
		fetch.setUserID(userID);
		return fetch;
	}
	
	@Override
	protected void onBindAndValidate(HttpServletRequest request,
			Object command, BindException errors) throws Exception {
		FetchAccount fetch = (FetchAccount) command;
		rejetMandatoryField("serverName", fetch.getServerName(), errors);
		rejetMandatoryField("userName", fetch.getUserName(), errors);
		rejetMandatoryField("password", fetch.getPassword(), errors);
	}
	
	private void rejetMandatoryField(String field, String value,
			BindException errors) {
		if (StringUtils.isEmpty(value)) {
			// Mandatory field
			errors.rejectValue(field, "field.required");
		}
	}
	
	private Store connect(String userName, String password)
			throws MessagingException {
		Session session = Session.getInstance(System.getProperties(), null);
		Store store = session.getStore("imap");
		store.connect("localhost", userName, password);
		return store;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		FetchAccount fetch = (FetchAccount) command;
		String userName = RequestUtils.getParameter(request, "destUserName");
		String password = RequestUtils.getParameter(request, "destPassword");
		Store store = null;
		try {
			store = connect(userName, password);
			FetchMailer mailer = new FetchMailer(fetch, store.getFolder("INBOX"));
			mailer.fetch();
			return new ModelAndView(getSuccessView(), errors.getModel());
		} catch (Exception e) {
			return showForm(request, response, errors);
		} finally {
			if (store != null) {
				store.close();
			}
		}
	}
	
}
