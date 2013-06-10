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

import java.io.Serializable;
import java.util.List;

/**
 * This class represents a BODYSTRUCTURE.
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 * 
 */
public class MimeDescriptor implements Serializable {

	private static final long serialVersionUID = -631959141687609724L;

	private long bodyOctets;	// Size in bytes
	private long lines;	// Size in lines
	private String type;	// Type
	private String subType;	// Subtype
	private String Id;	// Content-ID
	private String description;	// Content-Description
	private String encoding;	// Encoding
	private List<String> parameters;	// Body parameters
	private String disposition;	// Disposition
	private List<String> dispositionParams;	// Disposition parameters
	private List<String> languages;	// Language
	private String location;	// Location
	private String md5;	// MD-5 checksum
	private List<MimeDescriptor> parts;	// For multipart & message/rfc822
	private MimeDescriptor embeddedMessageDescriptor;	// For message/rfc822
	private Envelope envelope;	// For message/rfc822

	public MimeDescriptor(long bodyOctets, long lines, String type,
			String subType, String Id, String description, String encoding,
			List<String> parameters, String disposition,
			List<String> dispositionParams, List<String> languages,
			String location, String md5, List<MimeDescriptor> parts,
			MimeDescriptor embeddedMessageDescriptor, Envelope envelope) {
		super();
		this.bodyOctets = bodyOctets;
		this.lines = lines;
		this.type = type;
		this.subType = subType;
		this.Id = Id;
		this.description = description;
		this.encoding = encoding;
		this.parameters = parameters;
		this.disposition = disposition;
		this.dispositionParams = dispositionParams;
		this.languages = languages;
		this.location = location;
		this.md5 = md5;
		this.parts = parts;
		setEmbeddedMessageDescriptor(embeddedMessageDescriptor);
		setEnvelope(envelope);
	}

	public long getBodyOctets() {
		return bodyOctets;
	}

	public long getLines() {
		return lines;
	}

	public String getType() {
		return type;
	}

	public String getSubType() {
		return subType;
	}

	public String getId() {
		return Id;
	}

	public String getDescription() {
		return description;
	}

	public String getEncoding() {
		return encoding;
	}
	
	public List<String> getParameters() {
		return parameters;
	}

	public String getDisposition() {
		return disposition;
	}

	public List<String> getDispositionParams() {
		return dispositionParams;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public String getLocation() {
		return location;
	}

	public String getMd5() {
		return md5;
	}

	public List<MimeDescriptor> getParts() {
		return parts;
	}

	public void addPart(MimeDescriptor part) {
		parts.add(part);
	}
	
	public MimeDescriptor getEmbeddedMessageDescriptor() {
		return embeddedMessageDescriptor;
	}

	public void setEmbeddedMessageDescriptor(
			MimeDescriptor embeddedMessageDescriptor) {
		this.embeddedMessageDescriptor = embeddedMessageDescriptor;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}
	
}
