// Copyright (C) 2016 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

/**
 * Utility class for converting byte arrays and primitives to hexadecimal string representations.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.5
 */
public class Bytes {
    /**
     * Get a string representation of a byte array, where each byte is converted into a [0-F] hex character pair.
     */
    public static final String toHexString(byte[] b) {
	return toHexString(b, 0, b.length);
    }

    public static final String toHexString(byte[] b, int offset, int len) {
	int end = Math.min(b.length, (offset + len));
	StringBuffer sb = new StringBuffer();
	for (int i=offset; i < end; i++) {
	    sb.append(toHexString(b[i]));
	}
	return sb.toString();
    }

    public static final String toHexString(byte b) {
	return Integer.toString((b&0xff) + 0x100, 16).substring(1);
    }

    public static final String toHexString(short s) {
	return Integer.toHexString(s & 0x0000FFFF);
    }

    public static final String toHexString(int i) {
	return Integer.toHexString(i & 0xFFFFFFFF);
    }

    public static final String toHexString(long l) {
	return Long.toHexString(l & 0xFFFFFFFFFFFFFFFFL);
    }
}
