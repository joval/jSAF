// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;

import org.slf4j.cal10n.LocLogger;

import jsaf.intf.system.IEnvironment;
import jsaf.intf.system.IProcess;
import jsaf.intf.system.ISession;
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
    public Environment(ISession session) throws Exception {
	super();
	String lastKey = null;
	for (String line : SafeCLI.multiLine("set", session, ISession.Timeout.M)) {
	    int ptr = line.indexOf("=");
	    if (ptr > 0) {
		String key = line.substring(0,ptr);
		lastKey = key;
		String val = line.substring(ptr+1);
		props.setProperty(key.toUpperCase(), val);
	    } else if (lastKey != null) {
		String val = new StringBuffer(props.getProperty(lastKey.toUpperCase())).append(line).toString();
		props.setProperty(lastKey.toUpperCase(), val);
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
