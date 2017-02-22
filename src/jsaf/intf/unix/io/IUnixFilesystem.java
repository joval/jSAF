// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.io;

import java.io.IOException;

import jsaf.intf.io.IFilesystem;
import jsaf.intf.util.ISearchable;
import jsaf.intf.util.ISearchable.Condition;

/**
 * Defines extended attributes of a filesystem on Unix.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUnixFilesystem extends IFilesystem {
    /**
     * String delimiter for Unix filesystem paths.
     *
     * @since 1.0
     */
    String DELIM_STR = "/";

    /**
     * Character delimiter for Unix filesystem paths.
     *
     * @since 1.0
     */
    char DELIM_CH = '/';

    /**
     * ISearchable.Condition subclass for IUnixFilesystem searches.
     */
    public class UnixFSCondition extends FSCondition {
	/**
	 * Create a Condition for searching an IUnixFilesystem.
 	 */
	public UnixFSCondition(int field, int type, Object arg) {
	    super(field, type, arg);
	}

	/**
	 * Condition field for Unix file permissions. The only valid TYPE_ value is Condition.TYPE_EQUALITY.
	 * The value must be one of the enum values of FilePermission -- meaning that the condition makes an assertion
	 * about the user owner/group owner/other (i.e., world) permission setting on files that will be returned
	 * by the search. Multiple non-conflicting permission assertions can be made for any given search.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_PERM = 102;

	/**
	 * Condition field for Unix file user ownership. Supports the following condition types:
	 *   Condition.TYPE_EQUALITY - return files owned by the userID specified by the BigInteger value
	 *   Condition.TYPE_INEQUALITY - return files owned by userIDs not specified by the BigInteger value
	 *   Condition.TYPE_ANY - return files owned by any of the userIDs in the specified Collection&lt;BigInteger&gt; value
	 *   Condition.TYPE_NONE - return files owned by userIDs not in the specified Collection&lt;BigInteger&gt; value
	 *
	 * @since 1.2
	 */
	public static final int FIELD_USER = 103;

	/**
	 * Condition field for Unix file group ownership. Supports the following condition types:
	 *   Condition.TYPE_EQUALITY - return files owned by the groupID specified by the BigInteger value
	 *   Condition.TYPE_INEQUALITY - return files owned by groupIDs not specified by the BigInteger value
	 *   Condition.TYPE_ANY - return files owned by any of the groupIDs in the specified Collection&lt;BigInteger&gt; value
	 *   Condition.TYPE_NONE - return files owned by groupIDs not in the specified Collection&lt;BigInteger&gt; value
	 *
	 * @since 1.2
	 */
	public static final int FIELD_GROUP = 104;
    }

    /**
     * Enumeration of Unix file permissions, for use with the FIELD_PERM condition field.
     *
     * @since 1.3.5
     */
    enum FilePermission {
	UEXEC(0100),
	UREAD(0200),
	UWRITE(0400),
	GEXEC(010),
	GREAD(020),
	GWRITE(040),
	OEXEC(01),
	OREAD(02),
	OWRITE(04),
	SETUID(04000),
	SETGID(02000),
	STICKY(01000);

	private int bits;

	private FilePermission(int bits) {
	    this.bits = bits;
	}

	public int bits() {
	    return bits;
	}
    }
}
