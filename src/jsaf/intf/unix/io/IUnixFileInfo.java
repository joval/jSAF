// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.io;

import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;

import jsaf.intf.io.IFileEx;

/**
 * Defines extended attributes about a file on Unix.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUnixFileInfo extends IFileEx {
    /**
     * Native UNIX stat file type identifier for a directory.
     *
     * @since 1.0
     */
    char DIR_TYPE   = 'd';

    /**
     * Native UNIX stat file type identifier for a pipe.
     *
     * @since 1.0
     */
    char FIFO_TYPE  = 'p';

    /**
     * Native UNIX stat file type identifier for a link.
     *
     * @since 1.0
     */
    char LINK_TYPE  = 'l';

    /**
     * Native UNIX stat file type identifier for a block.
     *
     * @since 1.0
     */
    char BLOCK_TYPE = 'b';

    /**
     * Native UNIX stat file type identifier for a char.
     *
     * @since 1.0
     */
    char CHAR_TYPE  = 'c';

    /**
     * Native UNIX stat file type identifier for a socket.
     *
     * @since 1.0
     */
    char SOCK_TYPE  = 's';

    /**
     * Native UNIX stat file type identifier for a regular file.
     *
     * @since 1.0
     */
    char FILE_TYPE  = '-';

    /**
     * UNIX file type string, returned by getUnixFileType(), for a directory.
     *
     * @since 1.0
     */
    String FILE_TYPE_DIR	= "directory";

    /**
     * UNIX file type string, returned by getUnixFileType(), for a pipe.
     *
     * @since 1.0
     */
    String FILE_TYPE_FIFO	= "fifo";

    /**
     * UNIX file type string, returned by getUnixFileType(), for a link.
     *
     * @since 1.0
     */
    String FILE_TYPE_LINK	= "symlink";

    /**
     * UNIX file type string, returned by getUnixFileType(), for a block.
     *
     * @since 1.0
     */
    String FILE_TYPE_BLOCK	= "block";

    /**
     * UNIX file type string, returned by getUnixFileType(), for a char.
     *
     * @since 1.0
     */
    String FILE_TYPE_CHAR	= "character";

    /**
     * UNIX file type string, returned by getUnixFileType(), for a socket.
     *
     * @since 1.0
     */
    String FILE_TYPE_SOCK	= "socket";

    /**
     * UNIX file type string, returned by getUnixFileType(), for a regular file.
     *
     * @since 1.0
     */
    String FILE_TYPE_REGULAR	= "regular";

    /**
     * Returns the FILE_TYPE_* String constant describing the type of the file.
     *
     * @since 1.0
     */
    String getUnixFileType();

    /**
     * Get the whole permissions string, e.g., "rwxrwxrwx".
     *
     * @since 1.0
     */
    String getPermissions();

    /**
     * Get the integer ID of the user who owns this file.
     *
     * @since 1.0
     */
    int getUserId();

    /**
     * Get the integer ID of the group that owns this file.
     *
     * @since 1.0
     */
    int getGroupId();

    /**
     * Test the owner-read permission.
     *
     * @since 1.0
     */
    boolean uRead();

    /**
     * Test the owner-write permission.
     *
     * @since 1.0
     */
    boolean uWrite();

    /**
     * Test the owner-execute permission.
     *
     * @since 1.0
     */
    boolean uExec();

    /**
     * Test the set-UserID permission.
     *
     * @since 1.0
     */
    boolean sUid();

    /**
     * Test the group read permission.
     *
     * @since 1.0
     */
    boolean gRead();

    /**
     * Test the group write permission.
     *
     * @since 1.0
     */
    boolean gWrite();

    /**
     * Test the group execution permission.
     *
     * @since 1.0
     */
    boolean gExec();

    /**
     * Test the set-GroupID permission.
     *
     * @since 1.0
     */
    boolean sGid();

    /**
     * Test the anonymout read permission.
     *
     * @since 1.0
     */
    boolean oRead();

    /**
     * Test the anonymous write permission.
     *
     * @since 1.0
     */
    boolean oWrite();

    /**
     * Test the anonymous execution permission.
     *
     * @since 1.0
     */
    boolean oExec();

    /**
     * Test whether the file's mode bits are sticky.
     *
     * @since 1.0
     */
    boolean sticky();

    /**
     * Test whether the file has an extended ACL.
     *
     * @returns null if it is unknown whether or not the file has an extended ACL.
     * @since 1.0.1
     */
    Boolean hasPosixAcl();

    /**
     * Get the time the file's inode was last changed. Returns null if unknown.
     *
     * @since 1.2.1
     */
    public Date getLastChanged() throws IOException;
}
