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

public class PartContentBuilderTest extends TestCase {

	private PartContentBuilder normalBuilder;
	private PartContentBuilder zippedBuilder;
	
	protected void setUp() throws Exception {
		super.setUp();
		normalBuilder = new PartContentBuilder();
		zippedBuilder = new PartContentBuilder();
	}
	
	public void testBuild() throws Exception {
		Resource normal = new ClassPathResource("/text_html.eml");
		normalBuilder.build(normal.getInputStream(), null);

		File zipped = File.createTempFile("hwm", ".zip");
		FileUtils.compress(normal.getFile(), zipped);
		zippedBuilder.build(new GZIPInputStream(new FileInputStream(zipped)), null);
		zipped.delete();

		assertTrue(new EqualsBuilder().append(
				normalBuilder.getMessageBodyContent(),
				zippedBuilder.getMessageBodyContent()).isEquals());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
