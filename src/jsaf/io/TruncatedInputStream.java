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

    private long position, limit, mark;

    /**
     * Create a new TruncatedInputStream using raw data that has already been truncated.
     */
    public TruncatedInputStream(byte[] data) {
	super(new ByteArrayInputStream(data));
	limit = data.length;
	position = 0L;
    }

    /**
     * Truncate the wrapped InputStream at limit bytes. Once limit bytes have been read from the stream, the next
     * call to one of the read methods will throw a TruncatedIOException.  Note that if the stream ends after
     * exactly limit bytes, this class will still throw an exception at the end.  This is useful when reading from
     * a stream whose source has already been truncated.
     */
    public TruncatedInputStream(InputStream in, long limit) {
	super(in);
	if (limit == 0L) {
	    throw new IllegalArgumentException("limit=0");
	}
	this.limit = limit;
	position = 0L;
    }

    // Overrides

    @Override
    public int read() throws IOException {
	if (position < limit) {
	    int ch = in.read();
	    if (ch != -1) {
		position++;
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
	if (off < 0 || len < 0 || len > b.length - off) {
	    throw new IndexOutOfBoundsException();
	}
	if (position < limit) {
	    int bytesRead = in.read(b, off, (int)Math.min((long)len, limit - position));
	    position += (long)bytesRead;
	    return bytesRead;
	} else {
	    throw new TruncatedIOException(Message.getMessage(Message.ERROR_TRUNCATE, Long.toString(limit)));
	}
    }

    @Override
    public long skip(long n) throws IOException {
	if ((position + n) < limit) {
	    long skipped = in.skip(n);
	    position += skipped;
	    return skipped;
	} else {
	    throw new TruncatedIOException(Message.getMessage(Message.ERROR_TRUNCATE, Long.toString(limit)));
	}
    }

    @Override
    public void mark(int readlimit) {
	if (in.markSupported()) {
	    mark = position;
	    in.mark(readlimit);
	}
    }

    @Override
    public void reset() throws IOException {
	if (in.markSupported()) {
	    in.reset();
	    position = mark;
	}
    }
}
