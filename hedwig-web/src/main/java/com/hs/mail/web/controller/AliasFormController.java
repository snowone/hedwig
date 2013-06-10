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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.hs.mail.imap.user.Alias;
import com.hs.mail.imap.user.User;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.web.WebSession;
import com.hs.mail.web.util.RequestUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 1, 2010
 *
 */
public class AliasFormController extends SimpleFormController {

	private UserManager manager;
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
		long id = RequestUtils.getParameterLong(request, "ID", 0);
		if (id != 0) {
			return manager.getAlias(id);
		} else {
			return new Alias();
		}
	}

	@Override
	protected void onBindAndValidate(HttpServletRequest request,
			Object command, BindException errors) throws Exception {
		Alias alias = (Alias) command;
		String aliasName = RequestUtils.getParameter(request, "aliasName");
		if (StringUtils.isEmpty(aliasName)) {
			// Mandatory field
			errors.rejectValue("aliasName", "field.required");
		} else {
			domain = RequestUtils.getParameter(request, "domain");
			alias.setAlias(new StringBuffer(aliasName).append("@")
					.append(domain).toString());
		}
		if (StringUtils.isEmpty(alias.getUserID())) {
			// Mandatory field
			errors.rejectValue("userID", "field.required");
		} else {
			User deliverTo = manager.getUserByAddress(alias.getUserID());
			if (deliverTo == null) {
				errors.rejectValue("userID", "not.exist.address");
			} else {
				alias.setDeliverTo(deliverTo.getID());
			}
		}
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
			errors.rejectValue("aliasName", "address.alreay.exist");
			return showForm(request, response, errors);
		}
	}
	
	protected void doSubmitAction(WebSession session, Object command)
			throws Exception {
		Alias alias = (Alias) command;
		if (alias.getID() == 0) {
			session.removeBean(domain + WebSession.ALIAS_COUNT);
			manager.addAlias(alias);
		} else {
			manager.updateAlias(alias);
		}
	}

}
