// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * A platform-independent abstraction of a File.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IFile extends IFileMetadata {
    /**
     * Flags, used to specify the behavior of the IFile when it is initially retrieved.
     *
     * @since 1.0
     */
    enum Flags {
	/**
	 * Flag indicating that this IFile's information should never be cached.
	 *
	 * @since 1.0
	 */
	NOCACHE,

	/**
	 * Read-only access to a file that can be expected to change continuously (i.e., be growing in size).
	 *
	 * @since 1.0
	 */
	READVOLATILE,

	/**
	 * Simple read-only access to the IFile. Prohibits mkdir, getOutputStream, delete, and getRandomAccess("rw").
	 *
	 * @since 1.0
	 */
	READONLY,

	/**
	 * Read-write access to the IFile. Allows mkdir, getOutputStream, delete, and getRandomAccess("rw").
	 *
	 * @since 1.0
	 */
	READWRITE;
    }

    /**
     * Convenience method to get the part of the path following the last separator char.
     *
     * @since 1.0
     */
    public String getName();

    /**
     * Convenience method to get the part of the path preceding the last separator char.
     *
     * @since 1.0
     */
    public String getParent();

    /**
     * Returns whether the IFile exists.
     *
     * @since 1.0
     */
    boolean exists();

    /**
     * Returns whether the IFile represents a link to another IFile.
     *
     * @since 1.0
     */
    boolean isLink() throws IOException;

    /**
     * Does this file represent a directory?  Note, if this file is a link to a directory, this method is intended to
     * return true.
     *
     * @since 1.0
     */
    public boolean isDirectory() throws IOException;

    /**
     * Does this file represent a regular file (i.e., not a directory)?
     *
     * @since 1.0
     */
    public boolean isFile() throws IOException;

    /**
     * Create a directory at this IFile's path.
     *
     * @since 1.0
     */
    public boolean mkdir();

    /**
     * Get an InputStream of the file contents.
     *
     * @since 1.0
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Get an OutputStream to the file.  Start at the beginning (overwrite) or the end (append) as dictated.
     *
     * @since 1.0
     */
    public OutputStream getOutputStream(boolean append) throws IOException;

    /**
     * Get random access.
     *
     * @since 1.0
     */
    public IRandomAccess getRandomAccess(String mode) throws IllegalArgumentException, IOException;

    /**
     * For a directory, list the names of the subdirectories.
     *
     * @since 1.0
     */
    public String[] list() throws IOException;

    /**
     * For a directory, lists all the child files (Flags inherited).
     *
     * @since 1.0
     */
    public IFile[] listFiles() throws IOException;

    /**
     * For a directory, lists all the child files (Flags inherited) whose names match the specified pattern.
     *
     * @since 1.0
     */
    public IFile[] listFiles(Pattern p) throws IOException;

    /**
     * For a directory, retrieves an IFile for the child file with the specified name. Flags are inherited.
     *
     * @since 1.0
     */
    public IFile getChild(String name) throws IOException;

    /**
     * Delete the file.
     *
     * @since 1.0
     */
    public void delete() throws IOException;
}
