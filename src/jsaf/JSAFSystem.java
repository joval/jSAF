// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf;

import java.io.File;
import java.net.URL;
import java.util.Timer;

import jsaf.protocol.JSAFURLStreamHandlerFactory;

/**
 * This class is used to retrieve JSAF-wide resources, like the location of the JSAF workspace directory, and the
 * JSAF event system timer.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public final class JSAFSystem {
    private static Timer timer;
    private static File dataDir = null;
    private static boolean registeredHandlers = false;

    static {
	if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
	    String s = System.getenv("LOCALAPPDATA");
	    if (s == null) {
		s = System.getenv("APPDATA");
	    }
	    if (s != null) {
		File appDataDir = new File(s);
		dataDir = new File(appDataDir, "jSAF");
	    }
	}
	if (dataDir == null) {
	    File homeDir = new File(System.getProperty("user.home"));
	    dataDir = new File(homeDir, ".jSAF");
	}
	timer = new Timer("jSAF System Timer", true);
    }

    /**
     * Register the JSAF URL handlers for the tftp and zip protocols.
     */
    public static void registerURLHandlers() {
	if (!registeredHandlers) {
	    URL.setURLStreamHandlerFactory(JSAFURLStreamHandlerFactory.getInstance());
	    registeredHandlers = true;
	}
    }

    /**
     * Retrieve the daemon Timer used for scheduled jSAF tasks.
     */
    public static Timer getTimer() {
	return timer;
    }

    public static void setDataDirectory(File dir) throws IllegalArgumentException {
	if (dir.isDirectory()) {
	    dataDir = dir;
	} else {
	    String reason = Message.getMessage(Message.ERROR_IO_NOT_DIR);
	    throw new IllegalArgumentException(Message.getMessage(Message.ERROR_IO, dir.toString(), reason));
	}
    }

    /**
     * Return a directory suitable for storing transient application data, like state information that may persist
     * between invocations.  This is either a directory called .jSAF beneath the user's home directory, or on Windows,
     * it will be a directory named jSAF in the appropriate AppData storage location.
     */
    public static File getDataDirectory() {
	return dataDir;
    }
}
