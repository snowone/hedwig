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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.hs.mail.container.config.Config;
import com.hs.mail.util.FileUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Oct 2, 2010
 *
 */
public class MessageCompressor {
	
	static Logger logger = Logger.getLogger(MessageCompressor.class);

	private static final String DOT_GZIP_EXTENSION = "." + FileUtils.GZIP_EXTENSION;
	private static final String COMPRESSED = ".compressed";
	
	public MessageCompressor() {
		super();
	}
	
	public void compress(String prop, long timeLimit) {
		Date base = ScheduleUtils.getDateBefore(prop);
		if (base != null) {
			Date start = getStartDate(base);
			if (start != null) {
				Date date = start;
				if (logger.isDebugEnabled()) {
					logger.debug("Compressing directories from "
							+ DateFormatUtils.ISO_DATE_FORMAT.format(start)
							+ " to "
							+ DateFormatUtils.ISO_DATE_FORMAT.format(base));
				}
				while (date.before(base)
						&& System.currentTimeMillis() < timeLimit) {
					compressDirectories(date, timeLimit);
					date = DateUtils.addDays(date, 1);
				}
			}
		}
	}
	
	public static File getDataDirectory(Date date, long physmessageid) {
		return new File(Config.getDataDirectory(), Config.getSubDirectory(date,
				physmessageid));
	}
	
	private Date getStartDate(Date date) {
		while (true) {
			File dir = getDataDirectory(date, 0).getParentFile();
			if (dir.exists()) {
				File[] subdirs = dir.listFiles();
				for (int i = 0; i < subdirs.length; i++) {
					if (new File(subdirs[i], COMPRESSED).exists()) {
						return date;
					}
				}
			} else if (!dir.getParentFile().getParentFile().exists()) {
				return DateUtils.addDays(date, 1);
			}
			date = DateUtils.addDays(date, -1);
		}
	}
	
	private void compressDirectories(Date date, long timeLimit) {
		File directory = getDataDirectory(date, 0).getParentFile();
		if (directory.exists()) {
			File[] subdirs = directory.listFiles();
			for (int i = 0; i < subdirs.length
					&& System.currentTimeMillis() < timeLimit; i++) {
				if (!new File(subdirs[i], COMPRESSED).exists()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Start compressing "
								+ subdirs[i].getAbsolutePath());
					}
					compressDirectory(subdirs[i]);
				}
			}
		}
	}
	
	private void compressDirectory(File directory) {
		File[] files = directory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.endsWith(DOT_GZIP_EXTENSION);
			}
		});
		for (int i = 0; i < files.length; i++) {
			compressFile(files[i]);
		}
		FileUtils.createNewFile(new File(directory, COMPRESSED));
	}

	private void compressFile(File srcFile) {
		File destFile = null;
		try {
			destFile = new File(srcFile.getCanonicalPath() + DOT_GZIP_EXTENSION);
			FileUtils.compress(srcFile, destFile);
			srcFile.delete();
		} catch (IOException e) {
			if (srcFile.exists() && destFile != null && destFile.exists()) {
				destFile.delete();
			}
		}
	}
	
}
