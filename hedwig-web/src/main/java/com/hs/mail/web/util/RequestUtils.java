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
package com.hs.mail.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamSupport;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 17, 2007
 *
 */
public class RequestUtils {

	// logging
	private static Logger log = Logger.getLogger(RequestUtils.class);

	public static void debug(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		Enumeration names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String[] values = request.getParameterValues(name);
			sb.append(name).append('=');
			for (int i = 0; i < values.length; i++) {
				sb.append((i > 0) ? ',' + values[i] : values[i]);
			}
			sb.append("\r\n");
		}
		log.debug(sb.toString());
	}

	public static String getParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (value != null) {
			try {
				String encoding = request.getCharacterEncoding();
				// If the request.getCharacterEncoding() is null, the default
				// parsing value of the String is ISO-8859-1.
				return new String(
						value.getBytes((null == encoding) ? "ISO-8859-1"
								: encoding), "UTF-8");
			} catch (Exception ex) {
			}
		}
		return value;
	}

	public static String getParameter(HttpServletRequest request, String name,
			String defaultValue) {
		String value = getParameter(request, name);
		return (value != null) ? value : defaultValue;
	}

	public static boolean getParameterBool(HttpServletRequest request,
			String name) {
		String value = getParameter(request, name);
		return Boolean.valueOf(value).booleanValue();
	}

	public static boolean getParameterBool(HttpServletRequest request,
			String name, boolean defaultValue) {
		String value = getParameter(request, name, Boolean
				.toString(defaultValue));
		return Boolean.valueOf(value).booleanValue();
	}

	public static int getParameterInt(HttpServletRequest request, String name)
			throws Exception {
		String value = getParameter(request, name);
		if (value != null) {
			return toInt(value.trim());
		} else {
			throw new Exception("wma.number.format");
		}
	}

	public static int getParameterInt(HttpServletRequest request, String name,
			int defaultValue) {
		try {
			return getParameterInt(request, name);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static long getParameterLong(HttpServletRequest request, String name)
			throws Exception {
		String value = getParameter(request, name);
		if (value != null) {
			return toLong(value.trim());
		} else {
			throw new Exception("wma.number.format");
		}
	}

	public static long getParameterLong(HttpServletRequest request, String name,
			long defaultValue) {
		try {
			return getParameterLong(request, name);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static String[] getParameterValues(HttpServletRequest request,
			String name) {
		String[] values = request.getParameterValues(name);
		if (values != null) {
			try {
				String encoding = request.getCharacterEncoding();
				for (int i = 0; i < values.length; i++) {
					values[i] = (null == values[i]) 
						? null 
						: new String(values[i].getBytes((null == encoding) 
								? "ISO-8859-1" : encoding), "UTF-8");
				}
			} catch (Exception ex) {
			}
		}
		return values;
	}
	
	public static long[] getParameterLongs(HttpServletRequest request,
			String name) throws Exception {
		String[] values = request.getParameterValues(name);
		return (values != null) ? toLongs(values) : null;
	}

	public static String getReturnUrl(HttpServletRequest request,
			HttpServletResponse response) {
		ParamSupport.ParamManager params = new ParamSupport.ParamManager();
		Enumeration names = request.getParameterNames();
		String enc = response.getCharacterEncoding();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			if (!"returl".equals(name)) {
				String value = getParameter(request, name);
				try {
					params.addParameter(URLEncoder.encode(name, enc), 
							URLEncoder.encode(value, enc));
				} catch (UnsupportedEncodingException e) {
					params.addParameter(name, value);
				}
			}
		}
		String baseUrl = request.getRequestURL().toString();
		return params.aggregateParams(baseUrl);
	}

	public static int toInt(String number) throws Exception {
		try {
			return Integer.parseInt(number);
		} catch (Exception ex) {
			throw new Exception("wma.number.format");
		}
	}

	public static long toLong(String number) throws Exception {
		try {
			return Long.parseLong(number);
		} catch (Exception ex) {
			throw new Exception("wma.number.format");
		}
	}

	public static long[] toLongs(String[] values) throws Exception {
		long[] numbers = new long[values.length];
		for (int i = 0; i < values.length; i++) {
			numbers[i] = toLong(values[i].trim());
		}
		return numbers;
	}

}
