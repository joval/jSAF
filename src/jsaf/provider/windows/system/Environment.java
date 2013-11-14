// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.system;

import jsaf.intf.windows.system.IWindowsSession;
import jsaf.util.AbstractEnvironment;
import jsaf.util.SafeCLI;

/**
 * A representation of the Windows SYSTEM environment, retrieved from the set command.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Environment extends AbstractEnvironment {
    public Environment(IWindowsSession session) throws Exception {
	super(true);
	String lastKey = null;
	for (String line : SafeCLI.multiLine("set", session, IWindowsSession.Timeout.M)) {
	    int ptr = line.indexOf("=");
	    if (ptr > 0) {
		String key = line.substring(0,ptr);
		lastKey = key;
		String val = line.substring(ptr+1);
		map.put(key, val);
	    } else if (lastKey != null) {
		String val = new StringBuffer(map.get(lastKey)).append(line).toString();
		map.put(lastKey, val);
	    }
	}
    }

    /**
     * Determine whether this environment was sourced from a 64-bit Windows OS.
     */
    public boolean is64bit() {
	if (getenv(IWindowsSession.ENV_ARCH).indexOf("64") != -1) {
	    return true;
	} else {
	    String ar32 = getenv(IWindowsSession.ENV_AR32);
	    if (ar32 == null) {
		return false;
	    } else if (ar32.indexOf("64") != -1) {
		return true;
	    }
	}
	return false;
    }
}
