// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol.tftp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.net.tftp.TFTP;

import jsaf.Message;

/**
 * Stream handler class for TFTP URLs.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class TftpURLStreamHandler extends URLStreamHandler {
    public TftpURLStreamHandler() {
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
	if ("tftp".equals(url.getProtocol())) {
	    return new TftpURLConnection(url);
	} else {
	    throw new MalformedURLException(Message.getMessage(Message.ERROR_PROTOCOL, url.getProtocol()));
	}
    }

    @Override
    public int getDefaultPort() {
	return TFTP.DEFAULT_PORT;
    }
}
