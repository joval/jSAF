// Copyright (c) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimerTask;
import java.util.Vector;

import org.slf4j.cal10n.LocLogger;

import jsaf.JSAFSystem;
import jsaf.Message;
import jsaf.intf.io.IReader;
import jsaf.intf.util.IPerishable;
import jsaf.util.Strings;

/**
 * A PerishableReader is a class that implements both IReader and IPerishable, signifying input that has a potential to
 * expire.  Instances are periodically checked to see if they've been blocking on a read operation beyond the set expiration
 * timeout.  In that event, the underlying stream is closed so that the blocking Thread can continue.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class PerishableReader extends InputStream implements IReader, IPerishable {
    /**
     * Create a new instance using the given InputStream and initial timeout.  The clock begins ticking immediately, so
     * it is important to start reading before the timeout has expired.
     *
     * If the specified InputStream is already a PerishableReader, then its timeout is altered and it is returned.
     *
     * @arg maxTime the maximum amount of time that should be allowed to elapse between successful reads, in milliseconds.
     *              If maxTime <= 0, the default of 1hr will apply.
     */
    public static PerishableReader newInstance(InputStream in, long maxTime) {
	if (in == null) {
	    throw new NullPointerException();
	}
	PerishableReader reader = null;
	if (in instanceof PerishableReader) {
	    reader = (PerishableReader)in;
	    reader.setTimeout(maxTime);
	} else {
	    reader = new PerishableReader(in, maxTime);
	}
	return reader;
    }

    protected InputStream in;
    protected boolean isEOF;
    protected Buffer buffer;
    protected LocLogger logger;

    private boolean closed, expired;
    private long timeout;
    private TimerTask task;
    private StackTraceElement[] trace;

    // Implement ILoggable

    public LocLogger getLogger() {
	return logger;
    }

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    // Implement IReader

    public InputStream getStream() {
	return this;
    }

    @Override
    public void close() throws IOException {
	if (!closed)  {
	    in.close();
	    defuse();
	    closed = true;
	}
    }

    @Override
    public synchronized int available() throws IOException {
	int buffered = 0;
	if (buffer.hasNext()) {
	    buffered = buffer.len - buffer.pos;
	}
	return buffered + in.available();
    }

    @Override
    public boolean markSupported() {
	return true;
    }

    @Override
    public void mark(int readlimit) {
	setCheckpoint(readlimit);
    }

    @Override
    public void reset() throws IOException {
	restoreCheckpoint();
    }

    public synchronized boolean checkClosed() {
	return buffer.hasNext() || closed;
    }

    public synchronized boolean checkEOF() {
	return buffer.hasNext() || isEOF;
    }

    public synchronized String readLine() throws IOException {
	return readLine(Strings.ASCII);
    }

    public synchronized String readLine(Charset charset) throws IOException {
	ByteArrayOutputStream buff = new ByteArrayOutputStream();
	String result = null;
	int ch = 0;
	while(result == null && (ch = read()) != -1) {
	    switch(ch) {
	      case '\n':
		result = new String(buff.toByteArray(), charset);
		break;

	      case '\r':
		setCheckpoint(1);
		if (read() != '\n') {
		    restoreCheckpoint();
		}
		result = new String(buff.toByteArray(), charset);
		break;

	      default:
		buff.write((byte)ch);
		break;
	    }
	}
	if (result == null) {
	    defuse();
	    isEOF = true;
	    if (buff.size() > 0) {
		result = new String(buff.toByteArray(), charset);
	    }
	}
	return result;
    }

    public synchronized void readFully(byte[] buff) throws IOException {
	readFully(buff, 0, buff.length);
    }

    public synchronized void readFully(byte[] buff, int offset, int len) throws IOException {
	int end = offset + len;
	for (int i=offset; i < end; i++) {
	    int ch = read();
	    if (ch == -1) {
		defuse();
		isEOF = true;
		throw new EOFException(Message.getMessage(Message.ERROR_EOS));
	    } else {
		buff[i] = (byte)(ch & 0xFF);
	    }
	}
    }

    public synchronized byte[] readUntil(byte[] delim) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	boolean found = false;
	do {
	    byte[] buff = readUntil(delim[0]);
	    if (buff == null) {
		return null;
	    }
	    out.write(buff);
	    setCheckpoint(delim.length);
	    byte[] b2 = new byte[delim.length];
	    b2[0] = delim[0];
	    try {
		readFully(b2, 1, b2.length - 1);
		if (Arrays.equals(b2, delim)) {
		    found = true;
		} else {
		    out.write(b2[0]);
		    restoreCheckpoint();
		}
	    } catch (EOFException e) {
		restoreCheckpoint();
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

    public synchronized byte[] readUntil(int delim) throws IOException {
	int ch=0, len=0;
	byte[] buff = new byte[512];
	while((ch = read()) != -1 && ch != delim) {
	    if (len == buff.length) {
		byte[] old = buff;
		buff = new byte[old.length + 512];
		for (int i=0; i < old.length; i++) {
		    buff[i] = old[i];
		}
		old = null;
	    }
	    buff[len++] = (byte)(ch & 0xFF);
	}
	if (ch == -1 && len == 0) {
	    defuse();
	    isEOF = true;
	    return null;
	} else {
	    byte[] result = new byte[len];
	    for (int i=0; i < len; i++) {
		result[i] = buff[i];
	    }
	    return result;
	}
    }

    @Override
    public synchronized int read(byte[] buff) throws IOException {
	return read(buff, 0, buff.length);
    }

    @Override
    public synchronized int read(byte[] buff, int offset, int len) throws IOException {
	int bytesRead = 0;
	while (buffer.hasNext() && offset < buff.length) {
	    buff[offset++] = buffer.next();
	    bytesRead++;
	}
	bytesRead += in.read(buff, offset, len);
	int end = offset + bytesRead;
	for (int i=offset; buffer.hasCapacity() && i < end; i++) {
	    buffer.add((byte)(i & 0xFF));
	}
	resetTimer();
	return bytesRead;
    }

    @Override
    public synchronized int read() throws IOException {
	int i = -1;
	if (buffer.hasNext()) {
	    i = (int)buffer.next();
	} else if (!isEOF) {
	    i = in.read();
	    if (buffer.hasCapacity()) {
		buffer.add((byte)(i & 0xFF));
	    } else {
		buffer.clear(); // buffer overflow
	    }
	}
	if (i == -1) {
	    defuse();
	    isEOF = true;
	} else {
	    resetTimer();
	}
	return i;
    }

    public synchronized void setCheckpoint(int readAheadLimit) {
	buffer.init(readAheadLimit);
    }

    public synchronized void restoreCheckpoint() throws IOException {
	if (buffer.isEmpty()) {
	    throw new IOException("empty buffer");
	}
	buffer.reset();
    }

    // Implement IPerishable

    public synchronized boolean checkExpired() {
	return expired;
    }

    public synchronized void setTimeout(long timeout) {
	if (timeout <= 0) {
	    this.timeout = 3600000L; // 1hr
	} else {
	    this.timeout = timeout;
	}
	resetTimer();
    }

    /**
     * Kill the scheduled interrupt task and purge it from the timer.
     */
    public synchronized void defuse() {
	if (task != null) {
	    task.cancel();
	    task = null;
	}
	JSAFSystem.getTimer().purge();
    }

    // Protected

    protected synchronized void resetTimer() {
	defuse();
	task = new InterruptTask(Thread.currentThread());
	JSAFSystem.getTimer().schedule(task, timeout);
    }

    // Private

    protected PerishableReader(InputStream in, long timeout) {
	trace = Thread.currentThread().getStackTrace();
	if (in instanceof PerishableReader) {
	    PerishableReader input = (PerishableReader)in;
	    input.defuse();
	    this.in = input.in;
	    isEOF = input.isEOF;
	    closed = input.closed;
	    expired = input.expired;
	    buffer = input.buffer;
	    logger = input.getLogger();
	} else {
	    this.in = in;
	    isEOF = false;
	    closed = false;
	    expired = false;
	    buffer = new Buffer(0);
	    logger = Message.getLogger();
	}
	setTimeout(timeout);
	resetTimer();
    }

    class InterruptTask extends TimerTask {
	Thread t;

	InterruptTask(Thread t) {
	    this.t = t;
	}

	public void run() {
	    if (PerishableReader.this.isEOF) {
		try {
		    PerishableReader.this.close();
		} catch (IOException e) {
		}
	    } else if (!closed && t.isAlive()) {
		t.interrupt();
		PerishableReader.this.expired = true;

		//
		// These can be a pain to debug, so we log the stack trace documenting the history of this reader.
		//
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < trace.length; i++) {
		    sb.append(Strings.LOCAL_CR);
		    if (i > 0) {
			sb.append("    at ");
		    }
		    sb.append(trace[i].getClassName()).append(".").append(trace[i].getMethodName());
		    if (i > 0) {
			sb.append(" ").append(trace[i].getFileName()).append(", line: ").append(trace[i].getLineNumber());
		    }
		}
		logger.debug(Message.WARNING_PERISHABLEIO_INTERRUPT, sb.toString());
	    }
	    JSAFSystem.getTimer().purge();
	}
    }

    protected class Buffer {
	byte[] buff = null;
	int pos = 0;
	int len = 0;
	int resetPos = 0;

	public Buffer(int size) {
	    init(size);
	}

	@Override
	public String toString() {
	    if (buff == null) {
		return "Buffer empty";
	    }
	    return "Buffer size: " + buff.length + " pos: " + pos + " len: " + len +
		 " Ahead: \"" + new String(buff, pos, len - pos) + "\"" + " hasNext: " + hasNext();
	}

	void init(int size) {
	    if (hasNext()) {
		//
		// If the stream is already reading from inside the buffer, then don't lose the buffered data.
		//
		if (pos + size > len) {
		    byte[] temp = buff;
		    buff = new byte[size + pos];
		    len = len - pos;
		    System.arraycopy(temp, pos, buff, 0, len);
		    pos = 0;
		    resetPos = 0;
		}
	    } else {
		buff = new byte[size];
		len = 0;
		pos = 0;
		resetPos = 0;
	    }
	}

	public boolean isEmpty() {
	    return buff == null;
	}

	public void clear() throws IllegalStateException {
	    if (hasNext()) {
		throw new IllegalStateException(Integer.toString(len - pos));
	    } else if (buff != null) {
		buff = null;
	    }
	}

	public void reset() {
	    pos = resetPos;
	}

	public boolean hasNext() {
	    return buff != null && pos < len;
	}

	public byte next() throws NoSuchElementException {
	    if (hasNext()) {
		return buff[pos++];
	    } else {
		throw new NoSuchElementException();
	    }
	}

	public boolean hasCapacity() {
	    return buff != null && len < buff.length;
	}

	public void add(int ch) {
	    add((byte)(ch & 0xFF));
	}

	public void add(byte b) {
	    if (hasNext()) {
		//
		// A delayed add: insert the byte before the active part of the buffer
		//
		if (!hasCapacity()) {
		    init(buff.length + 1);
		}
		for (int i=len; i > pos; i--) {
		    buff[i] = buff[i-1];
		}
		len++;
		buff[pos++] = b;
	    } else if (hasCapacity()) {
		buff[len++] = b;
		pos = len;
	    } else {
		clear();
	    }
	}

	public void add(byte[] bytes, int offset, int length) {
	    int end = Math.min(bytes.length, (offset + length));
	    for (int i=offset; i < end; i++) {
		add(bytes[i]);
	    }
	}
    }
}
