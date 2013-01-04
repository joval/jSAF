// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;

import jsaf.intf.system.ISession;
import jsaf.provider.unix.system.UnixSession;
import jsaf.provider.windows.system.WindowsSession;

/**
 * Use this class to grab an IBaseSession for the local machine ONLY.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Local {
    public static ISession createSession(File dataDir) {
	File wsdir = new File(dataDir, ISession.LOCALHOST);
	if (!wsdir.isDirectory()) {
	    wsdir.mkdirs();
	}
	if (System.getProperty("os.name").startsWith("Windows")) {
	    return new WindowsSession(wsdir);
	} else {
	    return new UnixSession(wsdir);
	}
    }
}
