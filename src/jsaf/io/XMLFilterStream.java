// Copyright (C) 2016-2021, Arctic Wolf Networks, Inc.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.StringTokenizer;

import jsaf.util.Strings;

/**
 * InputStreams can contain characters that are illegal in XML. The XMLFilterStream replaces those characters with equivalent Unicode
 * escape sequences.
 *
 * @author David A. Solin
 * @since 1.6.10
 */
public class XMLFilterStream extends InputStream {
    private static final byte[] U8	= new byte[] {0x3C, 0x3F, 0x78, 0x6D}; // <?xm
    private static final byte[] U16	= new byte[] {0x00, 0x3C, 0x00, 0x3F}; // .<.?
    private static final byte[] U16LE	= new byte[] {0x3C, 0x00, 0x3F, 0x00}; // <.?.

    public static void main(String[] argv) throws Exception {
	Streams.copy(filterStream(new java.io.FileInputStream(argv[0]), true), System.out, false);
    }

    /**
     * Filter characters illegal in XML, provided the stream contains valid XML; i.e., equivalent to filterStream(in, false).
     */
    public static InputStream filterStream(InputStream in) throws IOException {
	return filterStream(in, false);
    }

    /**
     * Force filtering of characters illegal in XML from the specified stream.
     */
    public static InputStream filterStream(InputStream in, boolean force) throws IOException {
	if (!in.markSupported()) {
	    in = new BufferedInputStream(in);
	}
	in.mark(1024);
	Charset charset = null;

	//
	// First, attempt to use the BOM to determine the encoding
	//
	switch(in.read()) {
	  case 0xEF:
	    if (in.read() == 0xBB && in.read() == 0xBF) {
		charset = Strings.UTF8;
	    } else {
		throw new IllegalArgumentException("Invalid BOM");
	    }
	    break;
	  case 0xFE:
	    if (in.read() == 0xFF) {
		charset = Strings.UTF16;
	    } else {
		throw new IllegalArgumentException("Invalid BOM");
	    }
	    break;
	  case 0xFF:
	    if (in.read() == 0xFE) {
		charset = Strings.UTF16LE;
	    } else {
		throw new IllegalArgumentException("Invalid BOM");
	    }
	    break;
	  case -1:
	    in.reset();
	    return in;
	  default:
	    in.reset();
	    break;
	}

	//
	// If there was no BOM, attempt to determine the encoding from XML content
	//
	if (charset == null) {
	    byte[] buff = new byte[4];
	    switch(buff[0] = (byte)in.read()) {
	      case 0x3C:
		Streams.readFully(in, buff, 1, 3);
		break;
	      case 0x00:
		if ((buff[1] = (byte)in.read()) == 0x3C) {
		    Streams.readFully(in, buff, 2, 2);
		    break;
		} // else fall-thru
	      default: // not XML content
		buff = null;
		break;
	    }
	    if (buff != null) {
		if (Arrays.equals(buff, U8)) {
		    charset = Strings.UTF8;
		} else if (Arrays.equals(buff, U16)) {
		    charset = Strings.UTF16;
		} else if (Arrays.equals(buff, U16LE)) {
		    charset = Strings.UTF16LE;
		}
	    }
	    in.reset();
	}

	if (charset != null || force) {
	    return new XMLFilterStream(new InputStreamReader(in, charset == null ? Strings.UTF8 : charset));
	} else {
	    //
	    // Pass-thru (not XML or force-filtered)
	    //
	    return in;
	}
    }

    // Overrides for InputStream

    private InputStreamReader reader;
    private Charset charset;
    private byte[] buff = null;
    private int ptr = 0;
    private boolean closed=false, eof=false;

    @Override
    public int read() throws IOException {
	if (closed || eof) return -1;
	byte[] ch = new byte[1];
	if (read(ch, 0, 1) == -1) {
	    return -1;
	} else {
	    return (int)ch[0];
	}
    }

    @Override
    public int read(byte[] buffer) throws IOException {
	if (closed || eof) return -1;
	return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
	if (closed || eof) return -1;
	if (buff == null) {
	    char[] cbuff = new char[1024];
	    int len = reader.read(cbuff, 0, cbuff.length);
	    if (len == -1) {
		eof = true;
		return -1;
	    }
	    StringBuilder sb = new StringBuilder();
	    for (int i=0; i < len; i++) {
		char ch = cbuff[i];
		switch(ch) {
		  case 0x9:
		  case 0xA:
		  case 0xD:
		    sb.append(ch);
		    break;
		  default:
		    if (((ch >= 0x20) && (ch <= 0xD7FF)) || ((ch >= 0xE000) && (ch <= 0xFFFD)) || ((ch >= 0x010000) && (ch <= 0x10FFFF))) {
			sb.append(ch);
		    } else {
			sb.append("\\\\U").append(Integer.toString(ch + 0x10000, 16).substring(1)).append(";");
		    }
		    break;
		}
	    }
	    ptr = 0;
	    buff = sb.toString().getBytes(charset);
	    return read(buffer, offset, length);
	} else {
	    length = Math.min(buffer.length - offset, length);
	    int len = 0;
	    for (int i=0; i < length && ptr < buff.length; i++) {
		buffer[offset++] = buff[ptr++];
		len++;
	    }
	    if (ptr == buff.length) {
		buff = null;
	    }
	    return len;
	}
    }

    @Override
    public void close() throws IOException {
	reader.close();
	closed = true;
    }

    // Private

    private XMLFilterStream(InputStreamReader reader) {
	this.reader = reader;
	this.charset = Charset.forName(reader.getEncoding());;
    }
}
