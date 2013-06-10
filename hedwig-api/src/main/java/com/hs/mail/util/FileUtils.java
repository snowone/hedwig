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
package com.hs.mail.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * 
 * @author Won Chul Doh
 * @since Sep 27, 2010
 *
 */
public class FileUtils {
	
	public final static byte[] GZIP_MAGIC_BYTES = new byte[] { (byte) 0x1F,
			(byte) 0x8B };
	
	public final static String GZIP_EXTENSION = "zip";
	
	public static boolean startsWith(File file, byte[] magic) {
		InputStream input = null;
		try {
			int magicLen = magic.length;
			byte[] bytes = new byte[magicLen];
			input = new FileInputStream(file);
			input.read(bytes, 0, magicLen);
			return new EqualsBuilder().append(magic, bytes).isEquals();
		} catch (IOException e) {
			return false;
		} finally {
			IOUtils.closeQuietly(input);
		}
	}
	
	public static boolean isCompressed(File file, boolean checkMagic) {
		return (checkMagic) ? startsWith(file, GZIP_MAGIC_BYTES)
				: GZIP_EXTENSION.equalsIgnoreCase(FilenameUtils
						.getExtension(file.getName()));
	}
	
	public static boolean createNewFile(File file) {
		try {
			return file.createNewFile();
		} catch (IOException e) {
			return false;
		}
	}
	
	public static void compress(File srcFile, File destFile) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new BufferedInputStream(new FileInputStream(srcFile));
			output = new GZIPOutputStream(new FileOutputStream(destFile));
			IOUtils.copyLarge(input, output);
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);
		}
	}
	
	public static void uncompress(File srcFile, File destFile)
			throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new GZIPInputStream(new FileInputStream(srcFile));
			output = new BufferedOutputStream(new FileOutputStream(destFile));
			IOUtils.copyLarge(input, output);
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.exit(0);
		} else if ("-d".equals(args[0])) {
			uncompress(new File(args[1]), new File(args[2]));
		} else if ("-c".equals(args[0])) {
			compress(new File(args[1]), new File(args[2]));
		} else {
			System.exit(0);
		}
	}

}
