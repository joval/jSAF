// Copyright (C) 2014, jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

import jsaf.Message;

/**
 * An InputStream whose read methods throw an IOException after all available data has been read.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class TruncatedInputStream extends FilterInputStream {
    /**
     * An exception class that is thrown when a read is attempted after the stream limit has been reached.
     */
    public static class TruncatedIOException extends IOException {
	TruncatedIOException(String message) {
	    super(message);
	}
    }

    private long pointer, limit, mark;

    /**
     * Create a new TruncatedInputStream using raw data that has already been truncated.
     */
    public TruncatedInputStream(byte[] data) {
	super(new ByteArrayInputStream(data));
	limit = data.length;
	pointer = 0L;
    }

    /**
     * Truncate the wrapped InputStream at limit bytes.
     */
    public TruncatedInputStream(InputStream in, long limit) {
	super(in);
	if (limit == 0L) {
	    throw new IllegalArgumentException("limit=0");
	}
	this.limit = limit;
	pointer = 0L;
    }

    // Overrides

    @Override
    public int read() throws IOException {
	if ((pointer + 1L) < limit) {
	    int ch = in.read();
	    if (ch != -1) {
		pointer++;
	    }
	    return ch;
	} else {
	    throw new TruncatedIOException(Message.getMessage(Message.ERROR_TRUNCATE, Long.toString(limit)));
	}
    }

    @Override
    public int read(byte[] b) throws IOException {
	return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
	if ((pointer + 1L) < limit) {
	    int i = len;
	    while ((pointer + (long)i) > limit) {
		i--;
	    }
	    int r = in.read(b, off, i);
	    if (r > 0) {
		pointer += (long)r;
	    }
	    return r;
	} else {
	    throw new TruncatedIOException(Message.getMessage(Message.ERROR_TRUNCATE, Long.toString(limit)));
	}
    }

    @Override
    public long skip(long n) throws IOException {
	if ((pointer + n) < limit) {
	    long skipped = in.skip(n);
	    pointer += skipped;
	    return skipped;
	} else {
	    throw new TruncatedIOException(Message.getMessage(Message.ERROR_TRUNCATE, Long.toString(limit)));
	}
    }

    @Override
    public void mark(int readlimit) {
	mark = pointer;
	in.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
	in.reset();
	pointer = mark;
    }
}
