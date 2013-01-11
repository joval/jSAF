// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io.driver;

import org.slf4j.cal10n.LocLogger;

import jsaf.intf.io.IFilesystem;
import jsaf.intf.unix.io.IUnixFilesystemDriver;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.util.ILoggable;

abstract class AbstractDriver implements IUnixFilesystemDriver {
    protected IUnixSession session;
    protected LocLogger logger;

    AbstractDriver(IUnixSession session) {
	this.session = session;
	logger = session.getLogger();
    }

    protected class Mount implements IFilesystem.IMount {
	private String path, type;

	public Mount(String path, String type) {
	    this.path = path;
	    this.type = type;
	}

	// Implement IFilesystem.IMount

	public String getPath() {
	    return path;
	}

	public String getType() {
	    return type;
	}
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Internal

    /**
     * Get the number of times the character '/' appears in the path.
     */
    int getDepth(String path) {
	int depth = 0;
	int ptr = 0;
	while((ptr = path.indexOf("/", ptr)) != -1) {
	    ptr++;
	    depth++;
	}
	return depth;
    }
}
