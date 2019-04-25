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
    * Return rows of 16 hex-formatted bytes on the left, with ASCII format in a column on the right.
    */
   public static final String hexDump(byte[] data, int start, int len) {
	StringBuffer sb = new StringBuffer();
	int end = start+len;
	for (int i=start; i < end; i+=16) {
	    int term = Math.min(end, i+16);
	    int plus16 = i + 16;
	    for (int j=i; j < plus16; j++) {
		if (j < term) {
		    sb.append(Bytes.toHexString(data[j]));
		} else {
		    sb.append("  ");
		}
		sb.append(" ");
	    }
	    sb.append("  ");
	    byte[] buff = new byte[term - i];
	    for (int j=0; j < buff.length; j++) {
		int b = 0xFF & data[j+i];
		if (32 <= b && b <= 128) {
		    buff[j] = (byte)(0xFF & b);
		} else {
		    buff[j] = (byte)(0xFF & 254);
		}
	    }
	    sb.append(new String(buff, Strings.ASCII));
	    sb.append(Strings.LF);
	}
	return sb.toString();
    }

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

    public static final byte[] fromHexString(String s) throws IllegalArgumentException {
	int len = s.length();
	if (len % 2 == 1) {
	    throw new IllegalArgumentException(s);
	}
	byte[] data = new byte[len / 2];
	for (int i=0; i < data.length; i++) {
	    int charIndex = i * 2;
	    int msb = Character.digit(s.charAt(charIndex), 16);
	    int lsb = Character.digit(s.charAt(charIndex+1), 16);
	    data[i] = (byte) ((msb << 4) + lsb);
	}
	return data;
    }

    /**
     * Return a human-friendly string representation of the specified byte length.
     */
    public static String humanReadable(int length) {
	return humanReadable((long)length);
    }

    /**
     * Return a human-friendly string representation of the specified byte length.
     */
    public static String humanReadable(long length) {
	// See: http://programming.guide/java/formatting-byte-size-to-human-readable-format.html
	if (length < 1024) return length + " B";
	int exp = (int) (Math.log(length) / Math.log(1024));
	String pre = new StringBuffer().append("kMGTPE".charAt(exp-1)).toString();
	return String.format("%.1f %sB", length / Math.pow(1024, exp), pre);
    }
}
