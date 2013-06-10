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
package com.hs.mail.imap.processor.fetch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.MaximalBodyDescriptor;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeTokenStream;
import org.apache.james.mime4j.parser.RecursionMode;

import com.hs.mail.container.config.Config;
import com.hs.mail.io.CountingInputStream;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class BodyStructureBuilder {

	private EnvelopeBuilder envelopeBuilder;
	
	public BodyStructureBuilder(EnvelopeBuilder envelopeBuilder) {
		this.envelopeBuilder = envelopeBuilder;
	}
	
	public MimeDescriptor build(InputStream is) throws IOException,
			MimeException {
		MimeTokenStream parser = MimeParser.createMaximalDescriptorParser();
		parser.parse(is);
		parser.setRecursionMode(RecursionMode.M_NO_RECURSE);
		return createDescriptor(parser);
	}
	
	public MimeDescriptor build(Date date, long physmessageid)
			throws IOException, MimeException {
		File file = Config.getDataFile(date, physmessageid);
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			MimeDescriptor descriptor = build(is);
			writeBodyStructure(
					Config.getMimeDescriptorFile(date, physmessageid),
					descriptor);
			return descriptor;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public MimeDescriptor readBodyStructure(File file) {
		if (file.exists()) {
			ObjectInputStream is = null;
			try {
				InputStream in = new BufferedInputStream(new FileInputStream(
						file));
				is = new ObjectInputStream(in);
				return (MimeDescriptor) is.readObject();
			} catch (Exception ex) {
				// TODO - remove this file
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return null;
	}
	
	public void writeBodyStructure(File file, MimeDescriptor descriptor) {
		if (descriptor != null) {
			ObjectOutputStream os = null;
			try {
				OutputStream out = new BufferedOutputStream(
						new FileOutputStream(file));
				os = new ObjectOutputStream(out);
				os.writeObject(descriptor);
				os.flush();
			} catch (Exception e) {
				//
			} finally {
				IOUtils.closeQuietly(os);
			}
		}
	}
	
    private MimeDescriptor createDescriptor(MimeTokenStream parser)
			throws IOException, MimeException {
		int next = parser.next();
		Header header = new Header();
		while (next != MimeTokenStream.T_BODY
				&& next != MimeTokenStream.T_END_OF_STREAM
				&& next != MimeTokenStream.T_START_MULTIPART) {
			if (next == MimeTokenStream.T_FIELD) {
				header.addField(parser.getField());
			}
			next = parser.next();
		}
    	
    	switch (next) {
		case MimeTokenStream.T_BODY:
			return simplePartDescriptor(parser, header);
		case MimeTokenStream.T_START_MULTIPART:
			return compositePartDescriptor(parser, header);
		case MimeTokenStream.T_END_OF_STREAM:
            throw new MimeException("Premature end of stream");
		default:
            throw new MimeException("Unexpected parse state");
		}
	}
    
    private MimeDescriptor compositePartDescriptor(
			MimeTokenStream parser, Header header) throws IOException,
			MimeException {
		MaximalBodyDescriptor descriptor = (MaximalBodyDescriptor) parser
				.getBodyDescriptor();
		MimeDescriptor mimeDescriptor = createDescriptor(0, 0, descriptor,
				null, null);
		int next = parser.next();
		while (next != MimeTokenStream.T_END_MULTIPART
				&& next != MimeTokenStream.T_END_OF_STREAM) {
			if (next == MimeTokenStream.T_START_BODYPART) {
				mimeDescriptor.addPart(createDescriptor(parser));
			}
			next = parser.next();
		}
		return mimeDescriptor;
	}

	private MimeDescriptor simplePartDescriptor(MimeTokenStream parser,
			Header header) throws IOException, MimeException {
		MaximalBodyDescriptor descriptor = (MaximalBodyDescriptor) parser
				.getBodyDescriptor();
		if ("message/rfc822".equals(descriptor.getMimeType())) {
			CountingInputStream stream = new CountingInputStream(parser
					.getDecodedInputStream());
			MimeDescriptor embeddedMessageDescriptor = build(stream);
			Envelope envelope = createEnvelope(header);
			int octetCount = stream.getOctetCount();
			int lineCount = stream.getLineCount();
			return createDescriptor(octetCount, lineCount, descriptor,
					embeddedMessageDescriptor, envelope);
		} else {
			CountingInputStream stream = new CountingInputStream(parser
					.getInputStream());
			stream.readAll();
			int bodyOctets = stream.getOctetCount();
			int lines = stream.getLineCount();
			return createDescriptor(bodyOctets, lines, descriptor, null, null);
		}
	}
	
	private MimeDescriptor createDescriptor(long bodyOctets, long lines,
			MaximalBodyDescriptor descriptor,
			MimeDescriptor embeddedMessageDescriptor, Envelope envelope) {
		Map<String, String> parameters = new TreeMap<String, String>(descriptor
				.getContentTypeParameters());
		String charset = descriptor.getCharset();
		if (charset == null) {
			if ("TEXT".equalsIgnoreCase(descriptor.getMediaType())) {
				parameters.put("charset", "us-ascii");
			}
		} else {
			parameters.put("charset", charset);
		}
		String boundary = descriptor.getBoundary();
		if (boundary != null) {
			parameters.put("boundary", boundary);
		}
		MimeDescriptor mimeDescriptor = new MimeDescriptor(bodyOctets, lines, 
				descriptor.getMediaType(), 
				descriptor.getSubType(),
				descriptor.getContentId(),
				descriptor.getContentDescription(),
				descriptor.getTransferEncoding(),
				createParameters(parameters),
				descriptor.getContentDispositionType(),
				createParameters(descriptor.getContentDispositionParameters()),
				descriptor.getContentLanguage(),
				descriptor.getContentLocation(),
				descriptor.getContentMD5Raw(),
				new ArrayList<MimeDescriptor>(),
				embeddedMessageDescriptor,
				envelope);
		return mimeDescriptor;
	}
	
	private List<String> createParameters(Map<String, String> parameters) {
		if (MapUtils.isNotEmpty(parameters)) {
			List<String> results = new ArrayList<String>();
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				results.add(entry.getKey());
				results.add(entry.getValue());
			}
			return results;
		} else {
			return null;
		}
	}
	
	private Envelope createEnvelope(Header h) {
		Map<String, String> header = new HashMap<String, String>();
		List<Field> fields = h.getFields();
		if (!CollectionUtils.isEmpty(fields)) {
			for (Field field : fields) {
				header.put(field.getName(), field.getBody());
			}
		}
		return envelopeBuilder.build(header);
	}
		
}
