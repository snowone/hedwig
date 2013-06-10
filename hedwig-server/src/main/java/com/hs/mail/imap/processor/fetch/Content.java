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

import java.nio.ByteBuffer;

/**
 * 
 * @author Won Chul Doh
 * @since Mar 8, 2010
 *
 */
public class Content {
	
	private String name;

	private ByteBuffer contents;
	
	private long size;
	
	public Content(String name, ByteBuffer contents) {
		this.name = name;
		this.contents = contents;
		this.size = contents.limit();
	}

	public Content(String name, ByteBuffer contents, long size) {
		this.name = name;
		this.contents = contents;
		this.size = size;
	}
	
	public String getName() {
		return name;
	}
	
	public long getSize() {
		return size;
	}

	public ByteBuffer getContents() {
		return contents;
	}

}
