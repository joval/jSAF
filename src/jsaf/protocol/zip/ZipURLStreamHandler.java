// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol.zip;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import jsaf.Message;

/**
 * URLConnection subclass for generic ZIP files.  Only supports read requests.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0.1
 */
public class ZipURLStreamHandler extends URLStreamHandler {
    public ZipURLStreamHandler() {
    }

    public URLConnection openConnection(URL u) throws IOException {
        if ("zip".equals(u.getProtocol())) {
            return new ZipURLConnection(u);
        } else {
            throw new MalformedURLException(Message.getMessage(Message.ERROR_PROTOCOL, u.getProtocol()));
        }
    }
}
