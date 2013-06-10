package com.hs.mail.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LineReader extends InputStreamReader {

	private int maxLineLen = 2048;

	public LineReader(InputStream in) {
		super(in);
	}

	public String readLine() throws IOException {
		StringBuffer sb = new StringBuffer();
		int bytesRead = 0;
		while (bytesRead++ < maxLineLen) {
			int iRead = read();
			switch (iRead) {
			case '\r':
				iRead = read();
				if (iRead == '\n') {
					return sb.toString();
				}
				// fall through
			case '\n':
				// LF without a preceding CR
				throw new IOException("Bad line terminator");
			case -1:
				// premature EOF
				return null;
			default:
				sb.append((char) iRead);
			}
		}
		throw new IOException("Exceeded maximun line length");
	}

}
