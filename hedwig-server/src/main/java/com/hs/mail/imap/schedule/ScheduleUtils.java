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
package com.hs.mail.imap.schedule;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Oct 2, 2010
 *
 */
public class ScheduleUtils {

	public static Date getDateBefore(String str) {
		if (str != null) {
			int sz = str.length();
			for (int i = 0; i < sz; i++) {
				char ch = str.charAt(i);
				if (!Character.isDigit(ch)) {
					try {
						int amount = Integer.parseInt(str.substring(0, i));
						switch (Character.toUpperCase(ch)) {
						case 'D':
							return DateUtils.addDays(new Date(), -amount);
						case 'M':
							return DateUtils.addMonths(new Date(), -amount);
						case 'Y':
							return DateUtils.addYears(new Date(), -amount);
						}
					} catch (NumberFormatException e) {
						break;
					}
				}
			}
		}
		return null;
	}
	
	public static Date getTimeAfter(String str, Date defaultTime) {
		if (str != null) {
			int sz = str.length();
			for (int i = 0; i < sz; i++) {
				char ch = str.charAt(i);
				if (!Character.isDigit(ch)) {
					try {
						int amount = Integer.parseInt(str.substring(0, i));
						switch (Character.toUpperCase(ch)) {
						case 'H':
							return DateUtils.addHours(new Date(), amount);
						case 'M':
							return DateUtils.addMinutes(new Date(), amount);
						}
					} catch (NumberFormatException e) {
						break;
					}
				}
			}
		}
		return defaultTime;
	}
	
}
