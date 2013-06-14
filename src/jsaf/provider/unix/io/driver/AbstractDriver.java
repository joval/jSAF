// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.unix.io.IUnixFilesystemDriver;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.util.ILoggable;

abstract class AbstractDriver implements IUnixFilesystemDriver {
    protected static final String WILDCARD = ".*";

    protected Collection<IFilesystem.IMount> mounts;
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

    abstract void getMounts() throws Exception;

    // Implement IUnixFilesystemDriver

    public String getStatCommand() {
	throw new UnsupportedOperationException();
    }

    public Collection<IFilesystem.IMount> getMounts(Pattern typeFilter) throws Exception {
	return getMounts(typeFilter, false);
    }

    public Collection<IFilesystem.IMount> getMounts(Pattern typeFilter, boolean include) throws Exception {
	if (mounts == null) {
	    getMounts();
	}
	if (typeFilter == null) {
	    logger.debug(Message.STATUS_FS_MOUNT_FILTER, "[none]", "N/A");
	    for (IFilesystem.IMount mount : mounts) {
		logger.debug(Message.STATUS_FS_MOUNT_ADD, mount.getPath(), mount.getType());
	    }
	    return mounts;
	} else {
	    logger.debug(Message.STATUS_FS_MOUNT_FILTER, typeFilter.pattern(), Boolean.toString(include));
	    Collection<IFilesystem.IMount> results = new ArrayList<IFilesystem.IMount>();
	    for (IFilesystem.IMount mount : mounts) {
		if (typeFilter.matcher(mount.getType()).find()) {
		    if (include) {
			logger.debug(Message.STATUS_FS_MOUNT_ADD, mount.getPath(), mount.getType());
			results.add(mount);
		    } else {
			logger.debug(Message.STATUS_FS_MOUNT_SKIP, mount.getPath(), mount.getType());
		    }
		} else {
		    if (include) {
			logger.debug(Message.STATUS_FS_MOUNT_SKIP, mount.getPath(), mount.getType());
		    } else {
			logger.debug(Message.STATUS_FS_MOUNT_ADD, mount.getPath(), mount.getType());
			results.add(mount);
		    }
		}
	    }
	    return results;
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
     * Get the number of times the character '/' appears in the path, or 0 if the path is /.
     */
    int getDepth(String path) {
	if (path.equals("/")) return 0;
	int depth = 0;
	int ptr = 0;
	while((ptr = path.indexOf("/", ptr)) != -1) {
	    ptr++;
	    depth++;
	}
	return depth;
    }
}
