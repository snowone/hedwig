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
package com.hs.mail.container.config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.hs.mail.container.server.SSLContextFactory;
import com.hs.mail.util.InetAddressMatcher;

/**
 * Provides a number of properties to the server.
 * 
 * @author Won Chul Doh
 * @since Jun 3, 2010
 */
public class Config implements InitializingBean {
	
	static Logger console = Logger.getLogger("console");

	public static final String ZIPFILE_EXTENSION = "zip";
	public static final String MDCPOSTFIX = "__";
	
	private static final String DEF_CACHE_FIELDS = "Bcc,Cc,Date,From,In-Reply-To,Message-ID,Reply-To,Sender,Subject,To";

	private static DecimalFormat formatter = new DecimalFormat("0000000000");

	private static Properties properties;
	private static String authScheme;
	private static File dataDirectory;
	private static File tempDirectory;
	private static File spoolDirectory;
	private static Set<String> defaultCacheFields;
	private static long defaultQuota;
	private static String[] domains;
	private static String hostName;
	private static String helloName;
	private static InetAddressMatcher authorizedNetworks;
	private static String postmaster; 
	private static long maxMessageSize;
	private static int maxRcpt;
	private static boolean saslAuthEnabled;
	private static SSLContext context;
	private static boolean initData = true;

	public void setInitData(boolean initData) {
		Config.initData = initData;
	}

	public static Properties getProperties() {
		return properties;
	}
	
	public void setProperties(Properties properties) {
		Config.properties = properties;
	}

	public static String getAuthScheme() {
		return authScheme;
	}

	public static File getDataDirectory() {
		return dataDirectory;
	}

	public static String getSubDirectory(Date date, long physmessageid) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return new StringBuilder()
						.append("mail")
						.append(File.separator)
						.append(cal.get(GregorianCalendar.YEAR))
						.append(File.separator)
						.append(cal.get(GregorianCalendar.MONTH) + 1)
						.append(File.separator)
						.append(cal.get(GregorianCalendar.DAY_OF_MONTH))
						.append(File.separator)
						.append(Integer.parseInt(
								formatter.format(physmessageid)
									.substring(5, 8)))
						.toString();
	}
	
	public static File getDataFile(Date date, long physmessageid) throws IOException {
		File directory = new File(dataDirectory, getSubDirectory(date, physmessageid));
		FileUtils.forceMkdir(directory);
		File zipped = new File(directory, Long.toString(physmessageid)
				+ FilenameUtils.EXTENSION_SEPARATOR_STR + ZIPFILE_EXTENSION);
		return (zipped.exists()) ? zipped : new File(directory, Long
				.toString(physmessageid));
	}
	
	public static File getMimeDescriptorFile(Date date, long physmessageid) {
		File directory = new File(dataDirectory, getSubDirectory(date, physmessageid));
		return new File(directory, Long.toString(physmessageid) + MDCPOSTFIX);
	}

	public static File getTempDirectory() {
		return tempDirectory;
	}

	public static File getSpoolDirectory() {
		return spoolDirectory;
	}
	
	public static void setSpoolDirectory(File dir) {
		spoolDirectory = dir;
	}

	public static File getSnapshotDirectory() {
		return new File(spoolDirectory, "snapshot");
	}
	
	public static Set<String> getDefaultCacheFields() {
		return defaultCacheFields;
	}

	public static long getDefaultQuota() {
		return defaultQuota;
	}

	public static String[] getDomains() {
		return domains;
	}
	
	public static String getDefaultDomain() {
		return domains[0];
	}

	public static boolean isLocal(String domain) {
		return ArrayUtils.contains(domains, domain);
	}
	
	public static String getHostName() {
		return hostName;
	}

	public static String getHelloName() {
		return helloName;
	}
	
	public static InetAddressMatcher getAuthorizedNetworks() {
		return authorizedNetworks;
	}

	public static String getPostmaster() {
		return postmaster;
	}
	
	public static long getMaxMessageSize() {
		return maxMessageSize;
	}
	
	public static int getMaxRcptCount() {
		return maxRcpt;
	}
	
	public static boolean isSaslAuthEnabled() {
		return saslAuthEnabled;
	}
	
	public static SSLContext getSSLContext() {
		return context;
	}

	public static String getProperty(String key, String defaultValue) {
		return replaceByProperties(properties.getProperty(key, defaultValue));
	}
	
	public static File getFileProperty(String key, String file)
			throws IOException {
		return new File(getProperty(key, file));
	}

	public static long getNumberProperty(String key, long defaultValue) {
		try {
			String value = getProperty(key, Long.toString(defaultValue));
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public static boolean getBooleanProperty(String key, boolean defaultValue) {
		String value = getProperty(key, Boolean.toString(defaultValue));
		return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
	}

	public void afterPropertiesSet() throws Exception {
		dataDirectory = getFileProperty("data_directory", "${app.home}"
				+ File.separator + "data");
		console.info("Data directory is: " + dataDirectory.getCanonicalPath());
		
		tempDirectory = getFileProperty("temp_directory", "${app.home}"
				+ File.separator + "temp");
		console.info("Temp directory is: " + tempDirectory.getCanonicalPath());
		
		spoolDirectory = getFileProperty("queue_directory", "${app.home}"
				+ File.separator + "spool");
		console.info("Spool directory is: " + spoolDirectory.getCanonicalPath());
	
		authScheme = getProperty("auth_scheme", null);
		console.info("Authentication scheme is "
				+ ((authScheme != null) ? authScheme : "not specified"));
		
		String fields = getProperty("default_cache_fields", DEF_CACHE_FIELDS);
		defaultCacheFields = buildDefaultCacheFields(StringUtils.split(fields, ','));
		
		long quota = getNumberProperty("default_quota", 0);
		defaultQuota = quota * 1024 * 1024;
		console.info("Default quota is: " + quota + "MB");
		
		hostName = getProperty("myhostname", null);
		if (null == hostName) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException ex) {
				hostName = "localhost";
			}
		}
		console.info("Local host is: " + hostName);

		String domain = getProperty("mydomain", null);
		if (null == domain) {
			domains = new String[] { StringUtils.substringAfter(hostName, ".") };
		} else {
			domains = StringUtils.split(domain, ",");
		}
		for (int i = 0; i < domains.length; i++) {
			console.info("Handling mail for: " + domains[i]);
		}
		
		String networks = getProperty("mynetworks", "0.0.0.0/0.0.0.0");
		authorizedNetworks = new InetAddressMatcher(networks);
		console.info("SMTP relaying is allowded to: " + networks);
		
		helloName = getProperty("smtp_helo_name", hostName);
		
		postmaster = getProperty("postmaster", "postmaster");
		if (postmaster.indexOf('@') < 0) {
			String domainName = null;
			for (int i = 0; i < domains.length; i++) {
				String serverName = domains[i].toLowerCase(Locale.US);
				if (!"localhost".equals(serverName)) {
					domainName = serverName;
				}
			}
			postmaster = postmaster + "@"
					+ (domainName != null ? domainName : hostName);
		}
		console.info("Postmaster address is: " + postmaster);
		
		maxMessageSize = getNumberProperty("message_size_limit", 10240000);
		console.info("Maximum message size is: " + maxMessageSize);
		
		maxRcpt = (int) getNumberProperty("smtp_recipient_limit", 0);
		console.info("Maximun recipients count is: " + maxRcpt);
		
		saslAuthEnabled = getBooleanProperty("smtp_sasl_auth_enable", false);

		if (initData) {
			FileUtils.forceMkdir(dataDirectory);
			FileUtils.forceMkdir(tempDirectory);
			FileUtils.forceMkdir(spoolDirectory);
			FileUtils.forceMkdir(getSnapshotDirectory());
			buildSSLContext();
		}
	}
	
	private Set<String> buildDefaultCacheFields(String[] fields) {
		Set<String> result = new HashSet<String>();
		for (int i = 0; i < fields.length; i++) {
			result.add(fields[i]);
		}
		return result;
	}
	
	private void buildSSLContext() throws IOException {
		if (context == null) {
			String keyStore = getProperty("tls_keystore", null);
			if (keyStore != null) {
				String keyStorePassword = getProperty("tls_keypass", "");
				String certificatePassword = getProperty("tls_storepass", "");
				context = SSLContextFactory.createContext(keyStore,
						keyStorePassword, certificatePassword);
			}
		}
	}
	
	public static String replaceByProperties(String source) {
		if (null == source)
			return null;
		int end;
		int start = source.indexOf("${");
		while (-1 != start) {
			end = source.indexOf("}", start + 2);
			if (end > 0) {
				String propName = source.substring(start + 2, end);
				String propValue = System.getProperty(propName);
				if (null != propValue) {
					int propValueLen = propValue.length();
					source = new StringBuffer(source.length())
							.append(source.substring(0, start))
							.append(propValue)
							.append(source.substring(end + 1)).toString();
					start = source.indexOf("${", start + propValueLen);
				} else {
					start = source.indexOf("${", end + 1);
				}
			} else {
				break;
			}
		}
		return source;
	}
	
}
