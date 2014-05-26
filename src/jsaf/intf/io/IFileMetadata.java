// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Metadata about a file.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IFileMetadata {
    /**
     * Enumeration of basic file types.
     *
     * @since 1.0
     */
    enum Type {
	FILE,
	DIRECTORY,
	LINK;
    }

    /**
     * Obtain the file type.
     *
     * @since 1.0
     */
    Type getType() throws IOException;

    /**
     * If the IFile represents a link, returns a path to the link target.
     *
     * @since 1.0
     */
    String getLinkPath() throws IllegalStateException, IOException;

    /**
     * Get the size (in bytes) of this file.
     *
     * @since 1.0
     */
    public long length() throws IOException;

    /**
     * Returns the full path as it appears to the system on which the IFile resides (not necessarily canonical).
     *
     * @since 1.0
     */
    public String getPath();

    /**
     * Returns the canonical path representation of the IFile (i.e., linkless path).
     *
     * @since 1.0
     */
    public String getCanonicalPath() throws IOException;

    /**
     * Get a platform-specific extended attributes API, if any.
     *
     * @since 1.0
     */
    public IFileEx getExtended() throws IOException;

    /**
     * Get the time the file was last accessed. Returns null if unknown.
     *
     * @since 1.0.1
     */
    public Date getAccessTime() throws IOException;

    /**
     * Get the time the file was last modified. Returns null if unknown.
     *
     * @since 1.0.1
     */
    public Date getLastModified() throws IOException;

    /**
     * Get the time the file was created. Returns null if unknown.
     *
     * @since 1.0.1
     *
     * @deprecated in 1.2.1, when it was moved to the IWindowsFileInfo interface. On Windows this method is now equivalent to
     *             ((IWindowsFileInfo)getFileEx()).getCreateTime(), and on Unix it will throw an UnsupportedOperationException.
     */
    public Date getCreateTime() throws IOException;
}
