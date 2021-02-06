// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import jsaf.intf.util.IPublisher;
import jsaf.intf.util.Progress;

/**
 * InputStream that publishes Progress.UPDATE events as the underlying stream is read.
 *
 * @since 1.6.3
 * @author David A. Solin
 * @version %I% %G%
 */
public class ProgressInputStream extends InputStream {
    private IPublisher<Progress> publisher;
    private long length, bytesRead=0;
    private short lastPct = 0;
    private InputStream in;

    public ProgressInputStream(byte[] buff, IPublisher<Progress> publisher) throws IOException {
	this(new ByteArrayInputStream(buff), (long)buff.length, publisher);
    }

    public ProgressInputStream(CachedURLConnection conn, IPublisher<Progress> publisher) throws IOException {
	this(conn.getInputStream(), conn.getContentLengthLong(), publisher);
    }

    public ProgressInputStream(File f, IPublisher<Progress> publisher) throws IOException {
	this(new FileInputStream(f), f.length(), publisher);
    }

    @Override
    public int available() throws IOException {
	return in.available();
    }

    @Override
    public int read() throws IOException {
	int ch = in.read();
	if (ch != -1) {
	    update(1);
	}
	return ch;
    }

    @Override
    public int read(byte[] buff) throws IOException {
	return read(buff, 0, buff.length);
    }

    @Override
    public int read(byte[] buff, int offset, int length) throws IOException {
	int result = in.read(buff, offset, length);
	update(result);
	return result;
    }

    @Override
    public void close() throws IOException {
	in.close();
    }

    // Private

    private ProgressInputStream(InputStream in, long length, IPublisher<Progress> publisher) throws IOException {
	if (publisher == null) {
	    throw new NullPointerException();
	}
	this.publisher = publisher;
	this.in = in;
	this.length = length;
    }

    private void update(int len) {
	if (len > 0) {
	    bytesRead += len;
	    short pct = (short)((bytesRead * 100L) / length);
	    if (pct > lastPct) {
		publisher.publish(Progress.UPDATE, new Progress.Update(lastPct = pct, bytesRead));
	    }
	}
    }
}
