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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.user.Alias;
import com.hs.mail.imap.user.User;
import com.hs.mail.imap.user.UserManager;
import com.hs.mail.security.login.BasicCallbackHandler;
import com.hs.mail.web.WebSession;
import com.hs.mail.web.exception.KeyedException;
import com.hs.mail.web.exception.SessionRequiredException;
import com.hs.mail.web.util.DataImporter;
import com.hs.mail.web.util.Pager;
import com.hs.mail.web.util.RequestUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 1, 2010
 *
 */
public class WebConsole extends MultiActionController {
	
	// logging
	private final Logger logger = Logger.getLogger(getClass());

	private UserManager manager;
	
	public void setUserManager(UserManager userManager) {
		this.manager = userManager;
	}

	public ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RequestUtils.debug(request);
		return super.handleRequestInternal(request, response);
	}
	
	/** * Dispatchers ************************************************ */
	
	public ModelAndView session(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		WebSession session = new WebSession(request, response);
		String dome = request.getParameter("todo");
		return doDispatchSessionActions(session, request, response, dome);
	}

	public ModelAndView account(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		WebSession session = new WebSession(request, response);
		if (!session.isValid()) {
			throw new SessionRequiredException("session.required");
		}
		String dome = request.getParameter("todo");
		return doDispatchAccountActions(session, request, response, dome);
	}
	
	public ModelAndView alias(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		WebSession session = new WebSession(request, response);
		if (!session.isValid()) {
			throw new SessionRequiredException("session.required");
		}
		String dome = request.getParameter("todo");
		return doDispatchAliasActions(session, request, response, dome);
	}
	
	/** * End Dispatchers ************************************************ */

	/** * Session Actions ************************************************ */

	/**
	 * Dispatches actions targeting a <tt>session</tt>.
	 * 
	 * @param session
	 *            a <tt>WebSession</tt> instance
	 * @param request
	 *            a reference to the actual <tt>HttpServletRequest</tt> instance
	 * @param response
	 *            a reference to the actual <tt>HttpServletResponse</tt>
	 *            instance
	 * @param dome
	 *            the task as <tt>String</tt>
	 * @throws Exception
	 *             if it fails to dispatch the request to a method (i.e. invalid
	 *             request), or the action method fails to execute the task.
	 * 
	 */

	private ModelAndView doDispatchSessionActions(WebSession session,
			HttpServletRequest request, HttpServletResponse response,
			String dome) throws Exception {
		if ("login".equals(dome)) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String facility = request.getParameter("facility");
			return doLogin(session, username, password, facility);
		} else if ("logout".equals(dome)) {
			return doLogout(session, request);
		} else {
			throw new KeyedException("parameter.todo.invalid");
		}
	}
	
	private ModelAndView doLogin(WebSession session, String username,
			String password, String facility) {
		try {
			CallbackHandler callbackHandler = new BasicCallbackHandler(
					username, password.toCharArray());
			LoginContext lc = new LoginContext(facility, callbackHandler);
			lc.login();
			session.storeBean(WebSession.LOGIN_CONTEXT, lc);
			List<String> domains = Arrays.asList(Config.getDomains());
			ModelAndView mav = new ModelAndView("console");
			mav.addObject("domains", domains);
			return mav;
		} catch (LoginException e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("index", "error", "incorrect.password");
		}
	}
	
	private ModelAndView doLogout(WebSession session, HttpServletRequest request) {
		try {
			LoginContext lc = (LoginContext) session
					.retrieveBean(WebSession.LOGIN_CONTEXT);
			lc.logout();
			session.removeBean(WebSession.LOGIN_CONTEXT);
		} catch (LoginException e) {
			logger.error(e.getMessage(), e);
		}
		return new ModelAndView("index");
	}
	
	/*** End Session Actions ************************************************ */

	/** * Account Actions ************************************************ */

	private ModelAndView doDispatchAccountActions(WebSession session,
			HttpServletRequest request, HttpServletResponse response,
			String dome) throws Exception {
		if ("display".equals(dome)) {
			String domain = RequestUtils.getParameter(request, "domain");
			if (domain == null) {
				throw new KeyedException("parameter.missing.domain");
			}
			int page = RequestUtils.getParameterInt(request, "page", 1);
			int pageSize = RequestUtils.getParameterInt(request, "pageSize", 12);
			return doDisplayAccounts(session, domain, page, pageSize);
		} else if ("delete".equals(dome)) {
			String domain = RequestUtils.getParameter(request, "domain");
			if (domain == null) {
				throw new KeyedException("parameter.missing.domain");
			}
			long[] idarray = RequestUtils.getParameterLongs(request, "IDs");
			if (idarray == null) {
				idarray = RequestUtils.getParameterLongs(request, "ID");
				if (idarray == null) {
					throw new KeyedException("parameter.account.missinguid");
				}
			}
			return doDeleteAccounts(session, domain, idarray);
		} else if ("empty".equals(dome)) {
			long id = RequestUtils.getParameterLong(request, "ID", 0);
			if (id == 0) {
				throw new KeyedException("parameter.account.missinguid");
			}
			return doEmptyAccount(session, id);
		} else if ("doimport".equals(dome)) {
			return doImportAccounts(session);
		} else if ("import".equals(dome)) {
			return doDisplayImportAccounts();
		} else {
			throw new KeyedException("parameter.todo.invalid");
		}
	}

	private ModelAndView doDisplayAccounts(WebSession session, String domain,
			int page, int pageSize) {
		Integer count = (Integer) session.retrieveBean(domain + WebSession.ACCOUNT_COUNT);
		if (count == null) {
			count = manager.getUserCount(domain);
			session.storeBean(domain + WebSession.ACCOUNT_COUNT, count);
		}
		Pager pager = new Pager(page, pageSize, count, true);
		List<User> users = null;
		if (count > 0) {
			users = manager.getUserList(domain, page, pageSize);
		}
		ModelAndView mav = new ModelAndView("accountlist");
		mav.addObject("session", session);
		mav.addObject("users", users);
		mav.addObject("pager", pager);
		return mav;
	}

	private ModelAndView doDeleteAccounts(WebSession session, String domain,
			long[] idarray) {
		session.removeBean(domain + WebSession.ACCOUNT_COUNT);
		for (int i = 0; i < idarray.length; i++) {
			try {
				manager.deleteUser(idarray[i]);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return getRedirectView(session);
	}
	
	private ModelAndView doEmptyAccount(WebSession session, long id) {
		manager.emptyUser(id);
		return getRedirectView(session);
	}
	
	private ModelAndView doDisplayImportAccounts() {
		return new ModelAndView("import");
	}
	
	private ModelAndView doImportAccounts(WebSession session) {
		MultipartHttpServletRequest multi = (MultipartHttpServletRequest) session
				.getRequest();
		DataImporter importer = new DataImporter();
		MultipartFile mf = multi.getFile("file");
		if (mf != null) {
			try {
				session.removeBeans(WebSession.ACCOUNT_COUNT);
				importer.importAccount(manager, mf.getInputStream());
			} catch (IOException e) {
				importer.addError(0, mf.getOriginalFilename(), e);
			}
		}
		if (importer.hasErrors()) {
			return new ModelAndView("importerror", "errors", importer
					.getErrors());
		} else {
			return getRedirectView(session);
		}
	}
	
	/*** End Account Actions ************************************************ */

	/*** Alias Actions ************************************************ */

	private ModelAndView doDispatchAliasActions(WebSession session,
			HttpServletRequest request, HttpServletResponse response,
			String dome) throws Exception {
		if ("display".equals(dome)) {
			String domain = RequestUtils.getParameter(request, "domain");
			if (domain == null) {
				throw new KeyedException("parameter.missing.domain");
			}
			int page = RequestUtils.getParameterInt(request, "page", 1);
			int pageSize = RequestUtils.getParameterInt(request, "pageSize", 12);
			return doDisplayAliases(session, domain, page, pageSize);
		} else if ("delete".equals(dome)) {
			String domain = RequestUtils.getParameter(request, "domain");
			if (domain == null) {
				throw new KeyedException("parameter.missing.domain");
			}
			long[] idarray = RequestUtils.getParameterLongs(request, "IDs");
			if (idarray == null) {
				idarray = RequestUtils.getParameterLongs(request, "ID");
				if (idarray == null) {
					throw new KeyedException("parameter.alias.missinguid");
				}
			}
			return doDeleteAliases(session, domain, idarray);
		} else {
			throw new KeyedException("parameter.todo.invalid");
		}
	}
	
	private ModelAndView doDisplayAliases(WebSession session, String domain,
			int page, int pageSize) {
		Integer count = (Integer) session.retrieveBean(domain + WebSession.ALIAS_COUNT);
		if (count == null) {
			count = manager.getAliasCount(domain);
			session.storeBean(domain + WebSession.ALIAS_COUNT, count);
		}
		Pager pager = new Pager(page, pageSize, count, true);
		List<Alias> aliases = null;
		if (count > 0) {
			aliases = manager.getAliasList(domain, page, pageSize);
		}
		ModelAndView mav = new ModelAndView("aliaslist");
		mav.addObject("session", session);
		mav.addObject("aliases", aliases);
		mav.addObject("pager", pager);
		return mav;
	}

	private ModelAndView doDeleteAliases(WebSession session, String domain,
			long[] uids) {
		session.removeBean(domain + WebSession.ALIAS_COUNT);
		for (int i = 0; i < uids.length; i++) {
			try {
				manager.deleteAlias(uids[i]);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return getRedirectView(session);
	}
	
	/*** End Alias Actions ************************************************ */

	/*** Helper methods ****************************************************/

	private ModelAndView getRedirectView(WebSession session) {
		return new ModelAndView(new RedirectView(RequestUtils.getParameter(
				session.getRequest(), "returl")));
	}

}
