// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol.zip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jsaf.io.Streams;

/**
 * URLConnection subclass for generic ZIP files.  Only supports read requests.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0.1
 */
class ZipURLConnection extends URLConnection {
    private URL innerURL;
    private InputStream in;
    private int length;
    private String path;

    ZipURLConnection(URL url) throws MalformedURLException {
	super(url);
	String spec = url.toString();
	if (spec.startsWith("zip:")) {
	    int ptr = spec.indexOf("!/");
	    if (ptr > 4) {
		innerURL = new URL(spec.substring(4, ptr));
		path = spec.substring(ptr+2);
	    }
	}
	if (innerURL == null) {
	    throw new MalformedURLException(spec);
	}
    }

    // URLConnection overrides

    @Override
    public void connect() throws IOException {
	ZipInputStream zin = new ZipInputStream(innerURL.openStream());
	ZipEntry entry = advanceTo(zin, path);
	length = (int)entry.getSize();
	if (length <= 0) {
	    //
	    // We need to determine the uncompressed size
	    //
	    try {
		SizeStream out = new SizeStream();
		Streams.copy(zin, out);
		length = out.size();
	    } finally {
		Streams.close(zin);
	    }
	    zin = new ZipInputStream(innerURL.openStream());
	    advanceTo(zin, path);
	}
	in = zin;
	connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
	if (!connected) {
	    connect();
	}
	return in;
    }

    @Override
    public int getContentLength() {
	try {
	    if (!connected) {
		connect();
	    }
	    return length;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    // Private

    private static ZipEntry advanceTo(ZipInputStream zin, String name) throws IOException {
	ZipEntry entry;
	while ((entry = zin.getNextEntry()) != null) {
	    if (entry.getName().equals(name)) {
		return entry;
	    }
	}
	throw new FileNotFoundException(name);
    }

    private static class SizeStream extends OutputStream {
	int counter;

	SizeStream() {
	    counter = 0;
	}

	int size() {
	    return counter;
	}

	public void write(int b) {
	    counter++;
	}
    }
}
