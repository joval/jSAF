// Copyright (C) 2014-2017 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

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

    public InputStream getStream() {
	return in;
    }

    public String readLine() throws IOException {
	return readLine(Strings.UTF8);
    }

    public String readLine(Charset charset) throws IOException {
	byte[] buff = readUntilInternal('\n', false);
	if (buff.length == 0) {
	    return eof ? null : "";
	} else if (buff[buff.length - 1] == '\r') {
	    return new String(buff, 0, buff.length - 1, charset);
	} else {
	    return new String(buff, charset);
	}
    }

    public void readFully(byte[] buff) throws IOException {
	int total = 0;
	int len = 0;
	while((len = in.read(buff, total, buff.length - total)) > 0) {
	    total += len;
	    if (total == buff.length) {
		return;
	    }
	}
	eof = true;
	throw new EOFException();
    }

    public byte[] readUntil(int ch) throws IOException {
	return readUntilInternal(ch, true);
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

    public byte[] readUntilInternal(int ch, boolean throwEOF) throws IOException {
	ArrayList<Byte> list = new ArrayList<Byte>();
	while(true) {
	    int c = read();
	    if (eof && throwEOF) {
		throw new EOFException();
	    } else if (c == ch || eof) {
		byte[] buff = new byte[list.size()];
		for (int i=0; i < buff.length; i++) {
		    buff[i] = list.get(i).byteValue();
		}
		return buff;
	    } else {
		list.add(new Byte((byte)(0xFF & c)));
	    }
	}
    }
}
