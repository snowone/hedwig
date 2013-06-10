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

import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.hs.mail.imap.user.User;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.web.WebSession;
import com.hs.mail.web.util.MailUtils;
import com.hs.mail.web.util.RequestUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 1, 2010
 *
 */
public class AccountFormController extends SimpleFormController {

	private UserManager manager;
	private long uid;
	private String domain;
	
	public void setUserManager(UserManager userManager) {
		this.manager = userManager;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request) {
		String todo = request.getParameter("todo");
		return "docreate".equals(todo) || "doupdate".equals(todo);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		uid = RequestUtils.getParameterLong(request, "ID", 0);
		if (uid != 0) {
			return manager.getUser(uid);
		} else {
			return new User();
		}
	}

	@Override
	protected void onBindAndValidate(HttpServletRequest request,
			Object command, BindException errors) throws Exception {
		User user = (User) command;
		String userName = RequestUtils.getParameter(request, "userName");
		if (StringUtils.isEmpty(userName)) {
			// Mandatory field
			errors.rejectValue("userName", "field.required");
		} else {
			domain = RequestUtils.getParameter(request, "domain");
			user.setUserID(new StringBuffer(userName).append("@")
					.append(domain).toString());
		}
		if (StringUtils.isNotEmpty(user.getForwardTo())) {
			try {
				MailUtils.validateAddress(user.getForwardTo());
			} catch (ParseException e) {
				errors.rejectValue("forwardTo", "invalid.address");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map map = new HashMap();
		map.put("usage", (uid != 0) ? manager.getQuotaUsage(uid) : 0);
		return map;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		WebSession session = new WebSession(request, response);
		try {
			doSubmitAction(session, command);
			return new ModelAndView(getSuccessView(), errors.getModel());
		} catch (DataIntegrityViolationException ex) {
			// Unique key violation
			errors.rejectValue("userName", "address.alreay.exist");
			return showForm(request, response, errors);
		}
	}
	
	protected void doSubmitAction(WebSession session, Object command)
			throws Exception {
		User user = (User) command;
		if (user.getID() == 0) {
			session.removeBean(domain + WebSession.ACCOUNT_COUNT);
			manager.addUser(user);
		} else {
			manager.updateUser(user);
		}
	}

}
