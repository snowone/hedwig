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
package com.hs.mail.imap.message.response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.mail.FetchProfile;
import javax.mail.Flags;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.message.BodyFetchItem;
import com.hs.mail.imap.message.FetchData;
import com.hs.mail.imap.processor.fetch.BodyStructureBuilder;
import com.hs.mail.imap.processor.fetch.Content;
import com.hs.mail.imap.processor.fetch.Envelope;
import com.hs.mail.imap.processor.fetch.EnvelopeBuilder;
import com.hs.mail.imap.processor.fetch.MimeDescriptor;
import com.hs.mail.imap.processor.fetch.PartContentBuilder;
import com.hs.mail.util.FileUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class FetchResponseBuilder {
	
	private static Logger logger = Logger.getLogger(FetchResponseBuilder.class);
	
	private MailboxManager manager;
	private EnvelopeBuilder envelopeBuilder;
	private BodyStructureBuilder bodyStructureBuilder;

	public FetchResponseBuilder(MailboxManager manager) {
		this.manager = manager;
		this.envelopeBuilder = new EnvelopeBuilder();
		this.bodyStructureBuilder = new BodyStructureBuilder(this.envelopeBuilder);
	}
	
	public FetchResponse build(long msgnum, FetchProfile fp, FetchData fd) {
		FetchResponse response = new FetchResponse(msgnum);
		if (fp.contains(FetchProfile.Item.FLAGS)) {
			response.setFlags(fd.getFlags());
		}
		if (fp.contains(FetchData.FetchProfileItem.INTERNALDATE)) {
			response.setInternalDate(fd.getInternalDate());
		}
		if (fp.contains(FetchData.FetchProfileItem.SIZE)) {
			response.setSize(new Long(fd.getSize()));
		}
		if (fp.contains(FetchProfile.Item.ENVELOPE)) {
			Envelope envelope = buildEnvelope(fd.getPhysMessageID());
			response.setEnvelope(envelope);
		}
		if (fp.contains(FetchData.FetchProfileItem.BODY)
				|| fp.contains(FetchData.FetchProfileItem.BODYSTRUCTURE)) {
			MimeDescriptor descriptor = getBodyStructure(fd);
			if (fp.contains(FetchData.FetchProfileItem.BODY)) {
				response.setBody(descriptor);
			}
			if (fp.contains(FetchData.FetchProfileItem.BODYSTRUCTURE)) {
				response.setBodyStructure(descriptor);
			}
		}
		if (fp.contains(FetchData.FetchProfileItem.UID)) {
			response.setUid(new Long(fd.getMessageID()));
		}
		BodyFetchItem item = getBodyFetchItem(fp);
		if (item != null) {
			try {
				byte[] contents = bodyFetch(fd, item);
				Content content = buildBodyContent(contents, item);
				response.setContent(content);
				// Check if this fetch will cause the "SEEN" flag to be set
				// on this message.
				if (!item.isPeek()) {
					if (fd.getFlags() == null
							|| !fd.getFlags().contains(Flags.Flag.SEEN)) {
						manager.setFlags(fd.getMessageID(), new Flags(
								Flags.Flag.SEEN), false, true);
					}
				}
			} catch (Exception e) {
				// FIXME The main reason for this exception is that the message
				// file is not exist.
				// If we throw exception, all subsequent fetch will fail.
				// So, ignore this exception at now.
			}
		}
		return response;
	}
	
	private BodyFetchItem getBodyFetchItem(FetchProfile fp) {
		FetchProfile.Item[] items = fp.getItems();
		BodyFetchItem result = null;
		if (!ArrayUtils.isEmpty(items)) {
			for (int i = 0; i < items.length; i++) {
				if (items[i] instanceof BodyFetchItem) {
					result = (BodyFetchItem) items[i];
					break;
				}
			}
		}
		return result;
	}
	
	private Map<String, String> getHeader(long physmessageid) {
		return manager.getHeader(physmessageid);
	}
	
	private Map<String, String> getHeader(long physmessageid, String[] fields) {
		return manager.getHeader(physmessageid, fields);
	}

	private Envelope buildEnvelope(long physmessageid) {
		Map<String, String> header = getHeader(physmessageid,
				EnvelopeBuilder.WANTED_FIELDS);
		return envelopeBuilder.build(header);
	}
	
	private static InputStream getInputStream(FetchData fd) throws IOException {
		File file = Config.getDataFile(fd.getInternalDate(), fd.getPhysMessageID());
		if (FileUtils.isCompressed(file, false)) {
			return new GZIPInputStream(new FileInputStream(file));
		} else if (file.exists()) {
			return new BufferedInputStream(new FileInputStream(file));
		} else {
			return new ClassPathResource("/META-INF/notexist.eml").getInputStream();
		}
	}

	private MimeDescriptor getBodyStructure(FetchData fd) {
		File file = Config.getMimeDescriptorFile(fd.getInternalDate(),
				fd.getPhysMessageID());
		MimeDescriptor descriptor = bodyStructureBuilder
				.readBodyStructure(file);
		if (descriptor == null) {
			descriptor = buildBodyStructure(fd);
			if (descriptor != null) {
				bodyStructureBuilder.writeBodyStructure(file, descriptor);
			}
		}
		return descriptor;
	}
	
	private MimeDescriptor buildBodyStructure(FetchData fd) {
		InputStream is = null;
		try {
			is = getInputStream(fd);
			return bodyStructureBuilder.build(is);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private Content buildBodyContent(byte[] bytes, BodyFetchItem item) {
		long firstOctet = item.getFirstOctet();
		if (firstOctet >= 0) {
			long numberOfOctets = item.getNumberOfOctets();
			return buildPartialBodyContent(item.getName(), bytes, firstOctet,
					numberOfOctets);
		} else {
			return new Content(item.getName(), ByteBuffer.wrap(bytes));
		}
	}
	
	private Content buildPartialBodyContent(String name, byte[] bytes,
			long firstOctet, long numberOfOctets) {
		if (firstOctet >= bytes.length) {
			// If the starting octet is beyond the end of the text, an empty
			// string is returned.
			return new Content(name, ByteBuffer.wrap(PartContentBuilder.EMPTY));
		} else {
			if ((numberOfOctets < 0)
					|| ((firstOctet + numberOfOctets) > bytes.length)) {
				// If attempt to read beyond the end of the text, truncate as
				// appropriate.
				numberOfOctets = bytes.length - firstOctet;
			}
			StringBuilder sb = new StringBuilder(name).append('<').append(
					firstOctet).append('>');
			return new Content(sb.toString(), ByteBuffer.wrap(bytes,
					(int) firstOctet, (int) numberOfOctets), numberOfOctets);
		}
	}
	
	private byte[] bodyFetch(FetchData fd, BodyFetchItem item)
			throws IOException, MimeException {
		int[] path = item.getPath();
		try {
			if (ArrayUtils.isEmpty(path)) {
				return bodyContent(fd, item);
			} else {
				return bodyContent(fd, item, path, false);
			}
		} catch (PartContentBuilder.PartNotFoundException e) {
			// Missing parts should return zero sized content
			return PartContentBuilder.EMPTY;
		}
	}
	
	private byte[] bodyContent(FetchData fd, BodyFetchItem item)
			throws IOException, MimeException {
		long physmessageid = fd.getPhysMessageID();
		int specifier = item.getSectionType();
		String[] fields = item.getHeaders();
		switch (specifier) {
		case BodyFetchItem.HEADER:
		case BodyFetchItem.MIME:
			return addHeader(getHeader(physmessageid));
		case BodyFetchItem.HEADER_FIELDS:
			return addHeader(getHeader(physmessageid, fields), fields, false);
		case BodyFetchItem.HEADER_FIELDS_NOT:
			return addHeader(getHeader(physmessageid), fields, true);
		default:
			return bodyContent(fd, item, null, true);
		}
	}

	private byte[] bodyContent(FetchData fd, BodyFetchItem item, int[] path,
			boolean isBase) throws IOException, MimeException {
		InputStream is = null;
		try {
			is = getInputStream(fd);
			return bodyContent(is, item, path, isBase);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private byte[] bodyContent(InputStream is, BodyFetchItem item, int[] path,
			boolean isBase) throws IOException, MimeException {
		int specifier = item.getSectionType();
		String[] fields = item.getHeaders();
		switch (specifier) {
		case BodyFetchItem.HEADER:
			return addHeader(getHeader(is, path));
		case BodyFetchItem.HEADER_FIELDS:
			return addHeader(getHeader(is, path), fields, false);
		case BodyFetchItem.HEADER_FIELDS_NOT:
			return addHeader(getHeader(is, path), fields, true);
		case BodyFetchItem.MIME:
			return addHeader(getMimeHeader(is, path));
		case BodyFetchItem.TEXT:
			return addBodyContent(is, path);
		case BodyFetchItem.CONTENT:
		default:
			return addMimeBodyContent(is, path, isBase);
		}
	}
	
	private boolean contains(String[] fields, String name) {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	private void matching(String[] fields, Map<String, String> header,
			boolean not) {
		Object[] keys = header.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			boolean match = contains(fields, (String) keys[i]);
			if ((!not && !match) || (not && match)) {
				header.remove(keys[i]);
			}
		}
	}
	
	private Map<String, String> getHeader(InputStream is, int[] path)
			throws IOException, MimeException {
		PartContentBuilder builder = new PartContentBuilder();
		builder.build(is, path);
		Map<String, String> header = builder.getMessageHeader();
		return header;
	}

	private Map<String, String> getMimeHeader(InputStream is, int[] path)
			throws IOException, MimeException {
		PartContentBuilder builder = new PartContentBuilder();
		builder.build(is, path);
		return builder.getMimeHeader();
	}

	private byte[] addHeader(Map<String, String> header) {
		return toBytes(header);
	}

	private byte[] addHeader(Map<String, String> header, String[] fields,
			boolean not) {
		Map<String, String> clone = new HashMap<String, String>(header);
		matching(fields, clone, not);
		return toBytes(clone);
	}

	private byte[] addBodyContent(InputStream is, int[] path)
			throws IOException, MimeException {
		PartContentBuilder builder = new PartContentBuilder();
		builder.build(is, path);
		return builder.getMessageBodyContent();
	}

	private byte[] addMimeBodyContent(InputStream is, int[] path, boolean isBase)
			throws IOException, MimeException {
		if (isBase) {
			return IOUtils.toByteArray(is);
		} else {
			PartContentBuilder builder = new PartContentBuilder();
			builder.build(is, path);
			return builder.getMimeBodyContent();
		}
	}
	
	private byte[] toBytes(Map<String, String> header) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String value = entry.getValue(); 
			if (value != null) {
				sb.append(entry.getKey()).append(": ").append(entry.getValue())
						.append("\r\n");
			}
		}
		sb.append("\r\n");
		return sb.toString().getBytes();
	}
	
}
