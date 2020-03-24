// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol.memory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;

/**
 * URLConnection subclass for reading from memory. Only supports read requests.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class MemoryURLConnection extends URLConnection {
    MemoryURLConnection(URL url) {
	super(url);
    }

    // URLConnection overrides

    public void connect() {
	connected = true;
    }

    public InputStream getInputStream() throws IOException {
	if (!connected) {
	    connect();
	}
	byte[] data = MemoryURLFactory.getData(url);
	if (data == null) {
	    throw new IOException(Message.getMessage(Message.ERROR_MEMORY_URL_MAPPING, url.toString()));
	} else {
	    return new ByteArrayInputStream(data);
	}
    }
}
