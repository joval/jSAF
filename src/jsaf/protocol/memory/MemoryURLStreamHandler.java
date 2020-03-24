// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol.memory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import jsaf.Message;

/**
 * Stream handler class for data stored in-memory.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class MemoryURLStreamHandler extends URLStreamHandler {
    public MemoryURLStreamHandler() {
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
	if (url.getProtocol().equals("memory")) {
	    return new MemoryURLConnection(url);
	} else {
	    throw new MalformedURLException(Message.getMessage(Message.ERROR_PROTOCOL, url.getProtocol()));
	}
    }
}
