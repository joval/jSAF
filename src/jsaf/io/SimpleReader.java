// Copyright (C) 2014-2020 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.cal10n.LocLogger;

import jsaf.intf.io.IReader;
import jsaf.util.Strings;

/**
 * A simple IReader implementation for an InputStream.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class SimpleReader implements IReader {
    private InputStream in;
    private boolean closed, eof;
    private LocLogger logger;

    public SimpleReader(InputStream in) {
	this.in = in;
	this.logger = jsaf.Message.getLogger();
	closed = false;
	eof = false;
    }

    public SimpleReader(InputStream in, LocLogger logger) {
	this.in = in;
	this.logger = logger;
	closed = false;
	eof = false;
    }

    // Implement ILogger

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Implement IReader

    public int read() throws IOException {
	if (eof) {
	    return -1;
	} else {
	    int ch = in.read();
	    eof = ch == -1;
	    return ch;
	}
    }

    public int read(byte[] buff) throws IOException {
	return in.read(buff);
    }

    public InputStream getStream() {
	return in;
    }

    public String readLine() throws IOException {
	return readLine(Strings.UTF8);
    }

    public String readLine(Charset charset) throws IOException {
	byte[] buff = readUntilInternal('\n', false);
	if (buff == null || buff.length == 0) {
	    return eof ? null : "";
	} else if (buff[buff.length - 1] == '\r') {
	    return new String(buff, 0, buff.length - 1, charset);
	} else {
	    return new String(buff, charset);
	}
    }

    public void readFully(byte[] buff) throws IOException {
	readFully(buff, 0, buff.length);
    }

    public byte[] readUntil(int ch) throws IOException {
	return readUntilInternal(ch, true);
    }

    public synchronized byte[] readUntil(byte[] delim) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	boolean found = false;
	do {
	    byte[] buff = readUntilInternal(delim[0], false);
	    if (buff == null) {
		return null;
	    }
	    out.write(buff);
	    mark(delim.length);
	    byte[] b2 = new byte[delim.length];
	    b2[0] = delim[0];
	    try {
		readFully(b2, 1, b2.length - 1);
		if (Arrays.equals(b2, delim)) {
		    found = true;
		} else {
		    out.write(b2[0]);
		    reset();
		}
	    } catch (EOFException e) {
		reset();
		int len = 0;
		buff = new byte[512];
		while((len = read(buff)) > 0) {
		    out.write(buff, 0, len);
		}
		break;
	    }
	} while(!found);
	return out.toByteArray();
    }

    public void close() throws IOException {
	in.close();
	closed = true;
    }

    public boolean checkClosed() {
	return closed;
    }

    public boolean checkEOF() {
	return eof;
    }

    public void mark(int readLimit) {
	in.mark(readLimit);
    }

    public void reset() throws IOException {
	in.reset();
    }

    // Private

    private void readFully(byte[] buff, int offset, int length) throws IOException {
	int len = 0;
	while((len = in.read(buff, offset, length)) > 0) {
	    if (len == 0) {
		eof = true;
		throw new EOFException();
	    }
	    if ((offset += len) == buff.length) {
		return;
	    }
	}
    }

    public byte[] readUntilInternal(int ch, boolean throwEOF) throws IOException {
	ArrayList<Byte> list = new ArrayList<Byte>();
	while(true) {
	    int c = read();
	    if (c == ch || eof) {
		if (eof) {
		    if (throwEOF) {
			throw new EOFException();
		    } else if (list.size() == 0) {
			return null;
		    }
		}
		byte[] buff = new byte[list.size()];
		for (int i=0; i < buff.length; i++) {
		    buff[i] = list.get(i).byteValue();
		}
		return buff;
	    } else {
		list.add(Byte.valueOf((byte)(0xFF & c)));
	    }
	}
    }
}
