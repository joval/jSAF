// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.io;

import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;

import jsaf.intf.io.IFileEx;
import jsaf.intf.unix.identity.IGroup;
import jsaf.intf.unix.identity.IUser;

/**
 * Defines extended attributes about a file on Unix.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUnixFileInfo extends IFileEx {
    /**
     * Enumeration of Unix file types.
     *
     * @since 1.3
     */
    enum UnixType {
	DIR('d'),
	FIFO('p'),
	LINK('l'),
	BLOCK('b'),
	CHAR('c'),
	SOCK('s'),
	REGULAR('-');

	/**
	 * Get the UnixType corresponding to the specified stat char.
	 */
	public static UnixType getUnixType(char value) throws IllegalArgumentException {
	    for (UnixType type : values()) {
		if (type.value == value) {
		    return type;
		}
	    }
	    throw new IllegalArgumentException(new StringBuffer().append(value).toString());
	}

	/**
	 * Get the character used to represent this type in a stat().
	 */
	public char value() {
	    return value;
	}

	private char value;

	private UnixType(char value) {
	    this.value = value;
	}
    }

    /**
     * Returns the file's UnixType.
     *
     * @since 1.3
     */
    UnixType getUnixType();

    /**
     * An interface to Unix file permissions.
     *
     * @since 1.3
     */
    interface Permissions {
	/**
	 * Interface for a Unix file permissions group.
	 */
	interface Group {
	    boolean read();
	    boolean write();
	    boolean execute();
	}

	/**
	 * Get the permissions group for the user owner.
	 *
	 * @since 1.3
	 */
	Group user();

	/**
	 * Get the permissions group for group owner.
	 *
	 * @since 1.3
	 */
	Group group();

	/**
	 * Get the permissions group for all other users.
	 *
	 * @since 1.3
	 */
	Group world();

        /**
         * Get the whole permissions string, e.g., "rwxrwxrwx".
         *
         * @since 1.3
         */
	String toString();
    }

    /**
     * Access to file permissions.
     *
     * @since 1.3
     */
    Permissions getPermissions();

    /**
     * Get the user owner of the file.
     *
     * @since 1.3
     */
    IUser getUserOwner();

    /**
     * Get the group owner of the file.
     *
     * @since 1.3
     */
    IGroup getGroupOwner();

    /**
     * Test the set-UserID mode bit.
     *
     * @since 1.0
     */
    boolean sUid();

    /**
     * Test the set-GroupID mode bit.
     *
     * @since 1.0
     */
    boolean sGid();

    /**
     * Test the file's sticky mode bit.
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
     * @since 1.3
     */
    public Date getLastChanged() throws IOException;
}
