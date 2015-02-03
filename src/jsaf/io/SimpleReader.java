// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private BufferedReader reader;
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
	return in.read();
    }

    public InputStream getStream() {
	return in;
    }

    public String readLine() throws IOException {
	return readLine(Strings.UTF8);
    }

    public String readLine(Charset charset) throws IOException {
	if (reader == null) {
	    reader = new BufferedReader(new InputStreamReader(in, charset));
	}
	return reader.readLine();
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
	ArrayList<Byte> list = new ArrayList<Byte>();
	while(true) {
	    int c = read();
	    if (c == -1) {
		eof = true;
		throw new EOFException();
	    } else if (c == ch) {
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

    public void setCheckpoint(int readAheadLimit) throws IOException {
	in.mark(readAheadLimit);
    }

    public void restoreCheckpoint() throws IOException {
	in.reset();
    }
}
