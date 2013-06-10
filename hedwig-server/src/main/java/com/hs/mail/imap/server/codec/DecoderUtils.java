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
package com.hs.mail.imap.server.codec;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.mail.Flags;

import com.hs.mail.imap.message.SequenceRange;
import com.hs.mail.imap.message.request.Status;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 22, 2010
 *
 */
public class DecoderUtils {

	public static void decodeFlagList(String flagString, final Flags flags) {
		if (flagString.equalsIgnoreCase("\\Answered")) {
			flags.add(Flags.Flag.ANSWERED);
		} else if (flagString.equalsIgnoreCase("\\Flagged")) {
			flags.add(Flags.Flag.FLAGGED);
		} else if (flagString.equalsIgnoreCase("\\Deleted")) {
			flags.add(Flags.Flag.DELETED);
		} else if (flagString.equalsIgnoreCase("\\Seen")) {
			flags.add(Flags.Flag.SEEN);
		} else if (flagString.equalsIgnoreCase("\\Draft")) {
			flags.add(Flags.Flag.DRAFT);
		} else {
			if (flagString.equalsIgnoreCase("\\Recent")) {
				// RFC3501 specifically excludes /Recent
				// The /Recent flag should be set automatically by the server
			} else {
				// RFC3501 allows novel flags
				flags.add(flagString);
			}
		}
	}

	private static final DateFormat DATE_FORMAT0;
	private static final DateFormat DATE_FORMAT1;
	private static final DateFormat DATE_FORMAT2;
	static {
		DATE_FORMAT0 = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
		DATE_FORMAT1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss ZZZZ",
				Locale.US);
		DATE_FORMAT2 = new SimpleDateFormat(" d-MMM-yyyy HH:mm:ss ZZZZ",
				Locale.US);
	}

	public static Date parseDate(String date) throws ParseException {
		DateFormat df = DATE_FORMAT0;
		return df.parse(date);
	}

	public static Date parseDateTime(String dateTime) throws ParseException {
		String tz = dateTime.substring(21);
		tz = "GMT" + tz.charAt(0) + tz.charAt(1) + tz.charAt(2) + ":"
				+ tz.charAt(3) + tz.charAt(4);
		dateTime = dateTime.substring(0, 21) + tz;
		DateFormat df = (dateTime.charAt(0) != ' ') ? DATE_FORMAT1
				: DATE_FORMAT2;
		return df.parse(dateTime);
	}
	
	public static void decodeStatusAttList(String statusAtt,
			Status statusAtts) {
		String val = statusAtt.toUpperCase();
		if ("MESSAGES".equals(val)) {
			statusAtts.setMessages(true);
		} else if ("RECENT".equals(val)) {
			statusAtts.setRecent(true);
		} else if ("UIDNEXT".equals(val)) {
			statusAtts.setUidNext(true);
		} else if ("UIDVALIDITY".equals(val)) {
			statusAtts.setUidValidity(true);
		} else if ("UNSEEN".equals(val)) {
			statusAtts.setUnseen(true);
		}
	}
	
	public static SequenceRange parseSeqRange(String range) {
		int pos = range.indexOf(':');
		if (pos == -1) {
			long value = parseSeqNumber(range);
			return new SequenceRange(value);
		} else {
			long min = parseSeqNumber(range.substring(0, pos));
			long max = parseSeqNumber(range.substring(pos + 1));
			return new SequenceRange(Math.min(min, max), Math.max(min, max));
		}
	}
	
	private static long parseSeqNumber(String value) {
		if (value.length() == 1 && value.charAt(0) == '*') {
			return Long.MAX_VALUE;
		}
		return Long.parseLong(value);
	}

}
