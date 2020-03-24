// Copyright (C) 2020, JovalCM.com.  All rights reserved.

package jsaf.protocol.memory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jsaf.util.Checksum;

/**
 * A factory class for creating URLs pointing to in-memory data.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class MemoryURLFactory {
    private static final Map<String, byte[]> STORAGE = new HashMap<String, byte[]>();

    public static final URL createURL(byte[] data) throws MalformedURLException {
	String id = Checksum.getChecksum(data, Checksum.Algorithm.MD5);
	synchronized(STORAGE) {
	    STORAGE.put(id, data);
	}
	return new URL("memory", "localhost", 0, id);
    }

    public static final void destroyURL(URL url) {
	synchronized(STORAGE) {
	    STORAGE.remove(url.getFile());
	}
    }

    // Internal

    static byte[] getData(URL url) {
	return STORAGE.get(url.getFile());
    }
}
