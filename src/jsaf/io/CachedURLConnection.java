// Copyright (c) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.util.IDisposable;
import jsaf.util.Strings;

/**
 * A URLConnection that permits multiple calls to getInputStream, but only retrieves data from the URL once (unless
 * that url points to a file, in which case this class simply provides access to that file).
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.6.5
 */
public class CachedURLConnection extends URLConnection implements IDisposable {
    private File temp=null, original=null;
    private CachingStream stream = null;
    private LocLogger logger;

    public CachedURLConnection(URL url) {
	super(url);
	logger = Message.getLogger();
    }

    public void connect() throws IOException {
	if (!connected) {
	    if ("file".equals(url.getProtocol())) {
		try {
		    original = new File(url.toURI());
		} catch (URISyntaxException e) {
		    throw new IOException(e);
		}
	    } else {
		temp = File.createTempFile("url_cache", ".tmp");
	    }
	    connected = true;
	}
    }

    @Override
    public InputStream getInputStream() throws IOException {
	connect();
	if (original != null) {
	    return new FileInputStream(original);
	} else if (stream != null && stream.isEOF()) {
	    return new FileInputStream(temp);
	} else {
	    logger.debug(Message.STATUS_URL_CACHE, url.toString(), temp.toString());
	    return stream = new CachingStream(Streams.open(url));
	}
    }

    @Override
    public int getContentLength() {
	return (int)getContentLengthLong();
    }

    //@Override -- NB: override annotation is invalid when compiling with JDK 1.6
    public long getContentLengthLong() {
	try {
	    connect();
	    if (original != null) {
		return original.length();
	    } else {
		if (temp.length() == 0) {
		    // buffer the whole file
		    Streams.copy(Streams.open(url), new FileOutputStream(temp), true);
		}
		return temp.length();
	    }
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    // Implement IDisposable

    public void dispose() {
	if (temp != null) {
	    temp.delete();
	}
	url = null;
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Internal

    class CachingStream extends InputStream {
	private InputStream in;
	private boolean eof = false;

	CachingStream(InputStream in) throws IOException {
	    this.in = new StreamLogger(in, CachedURLConnection.this.temp);
	}

	public boolean isEOF() {
	    return eof;
	}

	public int read() throws IOException {
	    if (eof) {
		return -1;
	    } else {
		int ch = in.read();
		if (ch == -1) {
		    eof = true;
		    close();
		}
		return ch;
	    }
	}

	public int read(byte[] buff) throws IOException {
	    return read(buff, 0, buff.length);
	}

	public int read(byte[] buff, int offset, int len) throws IOException {
	    if (eof) {
		return -1;
	    } else {
		int result = in.read(buff, offset, len);
		if (result == -1) {
		    eof = true;
		}
		return result;
	    }
	}

	public void close() throws IOException {
	    in.close();
	}
    }
}
