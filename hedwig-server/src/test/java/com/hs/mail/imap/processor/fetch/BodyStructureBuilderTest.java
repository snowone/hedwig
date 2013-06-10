package com.hs.mail.imap.processor.fetch;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.hs.mail.util.FileUtils;

public class BodyStructureBuilderTest extends TestCase {

	private EnvelopeBuilder envelopeBuilder;
	private BodyStructureBuilder bodyStructureBuilder;

	protected void setUp() throws Exception {
		super.setUp();
		envelopeBuilder = new EnvelopeBuilder();
		bodyStructureBuilder = new BodyStructureBuilder(envelopeBuilder);
	}

	public void testBuild() throws Exception {
		Resource normal = new ClassPathResource("/text_html.eml");
		MimeDescriptor descriptorNormal = bodyStructureBuilder.build(normal
				.getInputStream());

		File zipped = File.createTempFile("hwm", ".zip");
		FileUtils.compress(normal.getFile(), zipped);
		MimeDescriptor descriptorZipped = bodyStructureBuilder
				.build(new GZIPInputStream(new FileInputStream(zipped)));
		zipped.delete();
			
		assertTrue(EqualsBuilder.reflectionEquals(descriptorNormal,
				descriptorZipped));
		assertTrue(descriptorNormal.getBodyOctets() == 1930
				&& descriptorNormal.getLines() == 25
				&& "text".equals(descriptorNormal.getType())
				&& "html".equals(descriptorNormal.getSubType()));
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
