// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.regex.Pattern;

import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.intf.util.ISearchable.Condition;

/**
 * A platform-independent abstraction of a server filesystem.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IFilesystem extends ILoggable {
    /**
     * A condition value indicating a regular file, for conditions of type FIELD_FILETYPE.
     *
     * @since 1.0
     */
    String FILETYPE_FILE = "f";

    /**
     * A condition value indicating a directory, for conditions of type FIELD_FILETYPE.
     *
     * @since 1.0
     */
    String FILETYPE_DIR = "d";

    /**
     * A condition value indicating a link, for conditions of type FIELD_FILETYPE.
     *
     * @since 1.0
     */
    String FILETYPE_LINK = "l";

    /**
     * Get the path delimiter character used by this filesystem.
     *
     * @since 1.0
     */
    String getDelimiter();

    /**
     * Retrieve an IFile with default (IFile.READONLY) access.
     *
     * @since 1.0
     */
    IFile getFile(String path) throws IOException;

    /**
     * Retrieve an IFile with the specified flags.
     *
     * @param flags IFile.READONLY, IFile.READWRITE, IFile.READVOLATILE, IFile.NOCACHE
     *
     * @since 1.0
     */
    IFile getFile(String path, IFile.Flags flags) throws IOException;

    /**
     * Retrieve multiple IFiles at once, all with default (IFile.READONLY) access. The order of the files corresponds to
     * the order of the path argument array. Note that any path that doesn't exist will have a null entry in its place in
     * the result.
     *
     * @since 1.0.1
     */
    IFile[] getFiles(String[] paths) throws IOException;

    /**
     * Retrieve multiple IFiles at once, all with the specified access. The order of the files corresponds to the
     * order of the path argument array. Note that if the flag is IFile.Flags.READONLY, any path that doesn't exist will
     * have a null entry in its place in the result.
     *
     * @param flags IFile.Flags.READONLY, IFile.Flags.READWRITE, IFile.Flags.READVOLATILE, IFile.Flags.NOCACHE
     *
     * @since 1.1
     */
    IFile[] getFiles(String[] paths, IFile.Flags flags) throws IOException;

    /**
     * Get random access to an IFile.
     *
     * @since 1.0
     */
    IRandomAccess getRandomAccess(IFile file, String mode) throws IllegalArgumentException, IOException;

    /**
     * Get random access to a file given its path (such as would be passed into the getFile method).
     *
     * @since 1.0
     */
    IRandomAccess getRandomAccess(String path, String mode) throws IllegalArgumentException, IOException;

    /**
     * Read a file.
     *
     * @since 1.0
     */
    InputStream getInputStream(String path) throws IOException;

    /**
     * Write to a file.
     *
     * @since 1.0
     */
    OutputStream getOutputStream(String path, boolean append) throws IOException;

    /**
     * Creates a new file in the specified directory, using the given prefix and suffix strings to generate its name. Any file created using this
     * method will be deleted (if not already deleted) when the ISession is disconnected.
     *
     * @param prefix The prefix string to be used in generating the file's name.
     * @param suffix The suffix string to be used in generating the file's name; may be null, in which case the suffix ".tmp" will be used.
     * @param directory The directory in which the file is to be created, or null if the default temporary file directory should be used.
     *
     * @throws IllegalArgumentException if directory.isDirectory() returns false.
     * @throws IOException if there is a problem creating the file.
     *
     * @since 1.3.1
     */
    IFile createTempFile(String prefix, String suffix, IFile directory) throws IOException;

    /**
     * Get all filesystem mounts.
     *
     * @since 1.0.1
     */
    Collection<IMount> getMounts() throws IOException;

    /**
     * An interface describing a filesystem mount point.
     *
     * @since 1.0
     */
    public interface IMount {
	/**
	 * Get the path of the mount.
	 *
	 * @since 1.0
	 */
	String getPath();

	/**
	 * Get the type of the mount. This is a platform-dependent String.
	 *
	 * @see jsaf.intf.windows.io.IWindowsFilesystem.FsType#value()
	 * @see <a href="http://www.kernel.org/doc/man-pages/online/pages/man2/mount.2.html">mount man page</a>
	 *
	 * @since 1.0
	 */
	String getType();

	/**
	 * Returns true if the mount is machine-local (i.e., not a mounted network device).
	 *
	 * @since 1.6.6
	 */
	boolean local();
    }

    /**
     * Access an ISearchable for the filesystem.
     *
     * @since 1.0
     */
    ISearchable<IFile> getSearcher();

    /**
     * A search condition for only matching directories.
     *
     * @since 1.2
     */
    FSCondition DIRECTORIES = new FSCondition(FSCondition.FIELD_FILETYPE, Condition.TYPE_EQUALITY, FILETYPE_DIR);

    /**
     * A search condition signifying that links should be followed in filesystem searches. The default behavior, if this
     * condition is not present, is to not follow links.
     *
     * @since 1.3.4
     */
    FSCondition FOLLOW_LINKS = new FSCondition(FSCondition.FIELD_FOLLOW_LINKS, Condition.TYPE_EQUALITY, Boolean.TRUE);

    /**
     * A search condition signifying that the search should be confined to the filesystem of the FROM condition. If this
     * condition is not present, the search can include results that reside in linked filesystems.
     *
     * @since 1.3.4
     */
    FSCondition XDEV = new FSCondition(FSCondition.FIELD_XDEV, Condition.TYPE_EQUALITY, Boolean.TRUE);

    /**
     * A search condition signifying that the search should be confined to local filesystems. If this condition is not
     * present, the search can include results that reside in networked filesystems.
     *
     * @since 1.6.6
     */
    FSCondition LOCAL = new FSCondition(FSCondition.FIELD_LOCAL, Condition.TYPE_EQUALITY, Boolean.TRUE);

    /**
     * Base ISearchable.Condition subclass for IFilesystem search conditions.
     *
     * @since 1.2
     */
    public class FSCondition extends Condition {
	/**
	 * Create a Condition for searching a generic IFilesystem.
	 */
	public FSCondition(int field, int type, Object arg) {
	    super(field, type, arg);
	}

	/**
	 * Condition field for a type (i.e., file/directory/link). Supports the following condition types:
	 *  TYPE_EQUALITY - only return files of type specified by the String value
	 *
	 * @since 1.0
	 */
	public static final int FIELD_FILETYPE = 50;

	/**
	 * Condition field for a file path pattern. Supports the following condition types:
	 *  TYPE_PATTERN - all files matching the java.util.regex.Pattern value
	 *
	 * @since 1.0
	 */
	public static final int FIELD_PATH = 51;

	/**
	 * Condition field for a file dirname (directory path) pattern. For files of type FILETYPE_DIR, the dirname is
	 * the same as the path. Supports the following condition types:
	 *  TYPE_PATTERN - search directories matching the java.util.regex.Pattern value
	 *  TYPE_EQUALITY - search the directory matching the String value
	 *  TYPE_ANY - search all directories matching the java.util.List&lt;String&gt; value
	 *
	 * @since 1.0
	 */
	public static final int FIELD_DIRNAME = 52;

	/**
	 * Condition field for a file basename (filename) pattern. Files of type FILETYPE_DIR have no basename. Supports
	 * the following condition types:
	 *  TYPE_PATTERN - search for files whose names match the java.util.regex.Pattern value
	 *  TYPE_EQUALITY - search for files whose names match the String value
	 *  TYPE_INEQUALITY - search for files whose names do not match the String value
	 *
	 * @since 1.0
	 */
	public static final int FIELD_BASENAME = 53;

	/**
	 * Condition field for a filesystem type. Supports the following condition types:
	 *  TYPE_EQUALITY - search only on filesystems matching the String value
	 *
	 * @since 1.0.1
	 */
	public static final int FIELD_FSTYPE = 54;

        /**
         * Condition field to search only local filesystems. Condition type and value are ignored.
	 *
         * @since 1.6.6
         */
        public static final int FIELD_LOCAL = 55;

        /**
         * Condition field for the link-following flag. Condition type and value are ignored. Links will not be followed
	 * in any filesystem search unless a condition with this field value is added.
	 *
	 * This field definition originally appeared in the IUnixFilesystem.UnixFSCondition in jSAF v1.2, but was migrated
	 * to this base class when support for Windows reparse points (AKA junctions) was added in 1.3.4.
         *
         * @since 1.3.4
         */
        public static final int FIELD_FOLLOW_LINKS = 100;

        /**
         * Condition field for the xdev flag (remain on filesystem). Condition type and value are ignored.
	 *
	 * This field definition originally appeared in the IUnixFilesystem.UnixFSCondition in jSAF v1.2, but was migrated
	 * to this base class when analogous support was added for Windows in 1.3.4.
         *
         * @since 1.3.4
         */
        public static final int FIELD_XDEV = 101;
    }
}
