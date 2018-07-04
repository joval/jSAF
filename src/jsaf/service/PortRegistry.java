// Copyright (C) 2018 JovalCM.com.  All rights reserved.

package jsaf.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Utility class for enumerating ports and names of well-known network services.
 *
 * @author David A. Solin
 * @version %I% %G$
 * @since 1.4
 */
public class PortRegistry {
    private static final Map<Integer, String> UDP, TCP;
    static {
	UDP = new HashMap<Integer, String>();
	TCP = new HashMap<Integer, String>();
	BufferedReader reader = null;
	try {
	    URL res = PortRegistry.class.getResource("etc.services");
	    reader = new BufferedReader(new InputStreamReader(res.openStream()));
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		line = line.trim();
		if (line.startsWith("#")) {
		    continue;
		}
		StringTokenizer tok = new StringTokenizer(line);
		if (tok.countTokens() >= 2) {
		    String name = tok.nextToken();
		    String portproto = tok.nextToken();
		    int ptr = portproto.indexOf("/");
		    if (ptr != -1) {
			int port = Integer.parseInt(portproto.substring(0,ptr));
			String proto = portproto.substring(ptr+1);
			if ("udp".equals(proto)) {
			    UDP.put(port, name);
			} else if ("tcp".equals(proto)) {
			    TCP.put(port, name);
			}
		    }
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} finally {
	    if (reader != null) {
		try {
		    reader.close();
		} catch (IOException e) {
		}
	    }
	}
    }

    /**
     * Return all the known UDP service entries.
     */
    public static Set<Map.Entry<Integer, String>> enumUdp() {
	return Collections.<Map.Entry<Integer, String>>unmodifiableSet(UDP.entrySet());
    }

    /**
     * Return all the known TCP service entries.
     */
    public static Set<Map.Entry<Integer, String>> enumTcp() {
	return Collections.<Map.Entry<Integer, String>>unmodifiableSet(TCP.entrySet());
    }
}
