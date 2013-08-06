// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io.fs;

import java.io.IOException;
import java.util.Date;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileEx;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.io.IRandomAccess;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.IProperty;
import jsaf.intf.util.ISearchable;
import jsaf.intf.system.ISession;
import jsaf.intf.system.IEnvironment;
import jsaf.util.StringTools;

/**
 * A DefaultMetadata object contains information about a file. It can be constructed from an IAccessor, or directly
 * from data gathered through other means (i.e., cached data). Subclasses are used to store platform-specific file
 * information.  Sublcasses should implement whatever extension of IFileEx is appropriate.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class DefaultMetadata implements IFileMetadata, IFileEx {
    protected String path, linkPath, canonicalPath;
    protected long length=-1L;
    protected Date ctime, mtime, atime;
    protected Type type = null;

    protected DefaultMetadata() {}

    public DefaultMetadata(Type type, String path, String linkPath, String canonicalPath, IAccessor a)
		throws IOException {

	this.type = type;
	this.path = path;
	this.linkPath = linkPath;
	this.canonicalPath = canonicalPath;
	ctime = a.getCtime();
	mtime = a.getMtime();
	atime = a.getAtime();
	length = a.getLength();
    }

    public DefaultMetadata(Type type, String path, String linkPath, String canonicalPath,
		Date ctime, Date mtime, Date atime, long length) {

	this.path = path;
	this.linkPath = linkPath;
	this.canonicalPath = canonicalPath;
	this.ctime = ctime;
	this.mtime = mtime;
	this.atime = atime;
	this.type = type;
	this.length = length;
    }

    // Implement IFileMetadata

    public Date getCreateTime() {
	return ctime;
    }

    public Date getLastModified() {
	return mtime;
    }

    public Date getAccessTime() {
	return atime;
    }

    public long length() {
	return length;
    }

    public Type getType() {
	return type;
    }

    public String getLinkPath() throws IllegalStateException, IOException {
	if (type == Type.LINK) {
	    return linkPath;
	} else {
	    throw new IllegalStateException(type.toString());
	}
    }

    public String getPath() {
	return path;
    }

    public String getCanonicalPath() {
	return canonicalPath;
    }

    public IFileEx getExtended() {
	return this;
    }
}

