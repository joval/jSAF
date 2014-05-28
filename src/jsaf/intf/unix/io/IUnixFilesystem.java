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
     * The Condition signifying that links should be followed in filesystem searches. The default behavior, if this
     * condition is not present, is to not follow links.
     *
     * @since 1.2
     */
    UnixFSCondition FOLLOW_LINKS =
	new UnixFSCondition(UnixFSCondition.FIELD_FOLLOW_LINKS, Condition.TYPE_EQUALITY, Boolean.TRUE);

    /**
     * The Condition signifying that the search should be confined to the filesystem of the FROM condition. If this
     * condition is not present, the search can include results that reside in linked filesystems.
     *
     * @since 1.2
     */
    UnixFSCondition XDEV =
	new UnixFSCondition(UnixFSCondition.FIELD_XDEV, Condition.TYPE_EQUALITY, Boolean.TRUE);

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
	 * Condition field for the link-following flag. Condition type and value are ignored.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_FOLLOW_LINKS = 100;

	/**
	 * Condition field for the xdev flag (remain on filesystem). Condition type and value are ignored.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_XDEV = 101;

	/**
	 * Condition field for Unix file permissions. The only valid TYPE_ value is Condition.TYPE_EQUALITY.
	 * The value must be of the form "[u/g/o][+/-][r/w/x]" -- meaning that the condition makes an assertion
	 * about the user owner/group owner/other (i.e., world) permission setting on files that will be returned
	 * by the search. Multiple non-conflicting permission assertions can be made for any given search.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_PERM = 102;

	/**
	 * Condition field for Unix file user ownership. Supports the following condition types:
	 *   Condition.TYPE_EQUALITY - return files owned by the userID specified by the Integer value
	 *   Condition.TYPE_INEQUALITY - return files owned by userIDs not specified by the Integer value
	 *   Condition.TYPE_ANY - return files owned by any of the userIDs in the specified Collection<Integer> value
	 *   Condition.TYPE_NONE - return files owned by userIDs not in the specified Collection<Integer> value
	 *
	 * @since 1.2
	 */
	public static final int FIELD_USER = 103;

	/**
	 * Condition field for Unix file group ownership. Supports the following condition types:
	 *   Condition.TYPE_EQUALITY - return files owned by the groupID specified by the Integer value
	 *   Condition.TYPE_INEQUALITY - return files owned by groupIDs not specified by the Integer value
	 *   Condition.TYPE_ANY - return files owned by any of the groupIDs in the specified Collection<Integer> value
	 *   Condition.TYPE_NONE - return files owned by groupIDs not in the specified Collection<Integer> value
	 *
	 * @since 1.2
	 */
	public static final int FIELD_GROUP = 104;
    }
}
