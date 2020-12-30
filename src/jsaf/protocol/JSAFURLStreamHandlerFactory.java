// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.protocol;

import java.io.File;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import jsaf.Message;
import jsaf.protocol.memory.MemoryURLStreamHandler;
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

    private static final Map<String, Delegate> handlers = new HashMap<String, Delegate>();
    static {
	handlers.put("tftp", new Delegate() {
	    public URLStreamHandler newHandler() {
		return new TftpURLStreamHandler();
	    }
	});
	handlers.put("zip", new Delegate() {
	    public URLStreamHandler newHandler() {
		return new ZipURLStreamHandler();
	    }
	});
	handlers.put("memory", new Delegate() {
	    public URLStreamHandler newHandler() {
		return new MemoryURLStreamHandler();
	    }
	});
    }

    /**
     * Get the singleton instance.
     */
    public static final URLStreamHandlerFactory getInstance() {
	return INSTANCE;
    }

    public interface Delegate {
	public URLStreamHandler newHandler();
    }

    public static void setDelegate(String name, Delegate delegate) {
	handlers.put(name, delegate);
    }

    // Implement URLStreamHandlerFactory

    public final URLStreamHandler createURLStreamHandler(String protocol) {
	if (handlers.containsKey(protocol)) {
	    return handlers.get(protocol).newHandler();
	} else {
	    return null;
	}
    }

    // Private

    private JSAFURLStreamHandlerFactory() {
    }
}
