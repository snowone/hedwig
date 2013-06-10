package com.hs.mail.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BASE64 {

	private BASE64() {
	}

	public static String encode(byte b[]) {
		StringBuffer sb = new StringBuffer((b.length / 3 + 1) * 4);
		int i = 0;
		int n = b.length;
		do {
			if (i >= n)
				break;
			sb.append(ALPHA[(b[i] & 0xff) >>> 2]);
			if (++i < n) {
				sb.append(ALPHA[(b[i - 1] & 3) << 4 | (b[i] & 0xff) >>> 4]);
			} else {
				sb.append(ALPHA[(b[i - 1] & 3) << 4]);
				sb.append("==");
				break;
			}
			if (++i < n) {
				sb.append(ALPHA[(b[i - 1] & 0xf) << 2 | (b[i] & 0xff) >>> 6]);
				sb.append(ALPHA[b[i] & 0x3f]);
			} else {
				sb.append(ALPHA[(b[i - 1] & 0xf) << 2]);
				sb.append('=');
				break;
			}
			i++;
		} while (true);
		return sb.toString();
	}

	public static byte[] decode(String s) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(
				(s.length() / 4) * 3);
		int cb[] = new int[4];
		int i = 0;
		int n = s.length();
		do {
			if (i >= n)
				break;
			int j = 0;
			do {
				if (j >= 4 || i >= n)
					break;
				int c = INDEX[s.charAt(i++)];
				if (c != -1)
					cb[j++] = c;
			} while (true);
			if (j < 4 && i < n)
				throw new IOException("Character buffer underflow");
			out.write(cb[0] << 2 | (cb[1] & 0x3f) >>> 4);
			if (j > 2)
				out.write((cb[1] & 0xf) << 4 | cb[2] >>> 2);
			if (j > 3)
				out.write((cb[2] & 3) << 6 | cb[3]);
		} while (true);
		return out.toByteArray();
	}

    private static final char ALPHA[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', '+', '/'
    };
 
	private static final int INDEX[];

	static {
		INDEX = new int[256];
		for (int i = 0; i < 256; i++)
			INDEX[i] = -1;

		for (int i = 0; i < ALPHA.length; i++)
			INDEX[ALPHA[i]] = i;
	}

}
