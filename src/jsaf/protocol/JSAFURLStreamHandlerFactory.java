// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import jsaf.protocol.tftp.TftpURLStreamHandler;
import jsaf.protocol.zip.ZipURLStreamHandler;

/**
 * A URLStreamHandlerFactory adding support for tftp and zip URLs.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public final class JSAFURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final JSAFURLStreamHandlerFactory INSTANCE = new JSAFURLStreamHandlerFactory();

    /**
     * Get the singleton instance.
     */
    public static final URLStreamHandlerFactory getInstance() {
	return INSTANCE;
    }

    // Implement URLStreamHandlerFactory

    public final URLStreamHandler createURLStreamHandler(String protocol) {
	if ("tftp".equals(protocol)) {
	    return new TftpURLStreamHandler();
	} else if ("zip".equals(protocol)) {
	    return new ZipURLStreamHandler();
	} else {
	    return null;
	}
    }

    // Private

    private JSAFURLStreamHandlerFactory() {}
}
