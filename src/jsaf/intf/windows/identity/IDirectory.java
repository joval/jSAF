// Copyright (C) 2011-2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import jsaf.identity.IdentityException;
import jsaf.intf.util.ISearchable;
import jsaf.intf.util.ISearchable.Condition;
import jsaf.intf.util.ILoggable;
import jsaf.provider.windows.identity.SID;

/**
 * Representation of a Windows user/group store.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IDirectory extends ILoggable {
    /**
     * Get the SID (Security Identifier) for the specified netbios name. The input parameter should be of the form DOMAIN\NAME. For built-in SIDs, the
     * DOMAIN\ part can be dropped, in which case the name parameter is just the principal name.
     *
     * @throws IllegalArgumentException if the domain is not recognized
     * @throws NoSuchElementException if the principal name does not map to a SID
     *
     * @since 1.5.0
     */
    SID lookupSID(String netbiosName) throws IllegalArgumentException, NoSuchElementException, IdentityException;

    /**
     * Returns a Principal (User or Group) given a SID.
     *
     * @throws NoSuchElementException if no principal exists for the specified SID value
     *
     * @since 1.5.0
     */
    IPrincipal getPrincipal(SID sid) throws NoSuchElementException, IdentityException;

    /**
     * Filter interface for the enumerateLocalPrincipals method.
     *
     * @since 1.5.0
     */
    interface PrincipalFilter {
	public boolean accept(IPrincipal principal);
    }

    PrincipalFilter USER_FILTER = new PrincipalFilter() {
	public boolean accept(IPrincipal principal) {
	    return principal.getType() == IPrincipal.Type.USER;
	}
    };

    PrincipalFilter GROUP_FILTER = new PrincipalFilter() {
	public boolean accept(IPrincipal principal) {
	    return principal.getType() == IPrincipal.Type.GROUP;
	}
    };

    PrincipalFilter WILDCARD_FILTER = new PrincipalFilter() {
	public boolean accept(IPrincipal principal) {
	    return true;
	}
    };

    /**
     * Returns all the principals in the LSA, with the specified filter. This can be an expensive operation, particularly on a domain controller, if the
     * filter attempts to retrieve anything other than the type, SID, or Netbios name.
     *
     * @since 1.5.0
     */
    Collection<IPrincipal> enumerateLocalPrincipals(PrincipalFilter filter) throws IdentityException;

    /**
     * Returns the SID for the local machine.
     *
     * @since 1.5.0
     */
    SID getComputerSid() throws IdentityException;

    /**
     * Resolve all members of the specified group, including sub-groups and their members, recursively.
     *
     * @since 1.5.0
     */
    Collection<IPrincipal> getAllMembers(IGroup group) throws IdentityException;

    /**
     * Does the LSA (Local Security Authority) recognize this SID?
     *
     * @since 1.5.0
     */
    boolean isLocal(SID sid) throws IdentityException;

    /**
     * Access an ISearchable for the Local Security Authority.
     *
     * @since 1.5.0
     */
    ISearchable<IPrincipal> getSearcher() throws IdentityException;

    /**
     * A search condition for retrieving all the service SIDs.
     *
     * @since 1.5.0
     */
    DirCondition SERVICES = new DirCondition(DirCondition.FIELD_SID, Condition.TYPE_PATTERN, Pattern.compile("^S-1-5-80-"));

    /**
     * Base ISearchable.Condition subclass for IDirectory search conditions.
     *
     * @since 1.5.0
     */
    public static final class DirCondition extends Condition {
	/**
	 * Create a Condition for searching a windows IDirectory.
	 */
	public DirCondition(int field, int type, Object arg) {
	    super(field, type, arg);
	}

	/**
	 * Condition field for a SID pattern. Supports the following condition types:
	 *  TYPE_PATTERN - search for a SID matching the java.util.regex.Pattern value
	 *  TYPE_ANY - retrieve multiple IPrincipals given a java.util.Collection&lt;SID&gt;
	 *
	 * @since 1.5.0
	 */
	public static final int FIELD_SID = 1000;

	/**
	 * Condition field for a principal name (as in, the String returned by IPrincipal.getName()). 
	 * Supports the following condition types:
	 *  TYPE_PATTERN - search for a principal name matching the java.util.regex.Pattern value
	 *  TYPE_ANY - retrieve multiple IPrincipals given a java.util.Collection&lt;String&gt;
	 *
	 * @since 1.5.0
	 */
	public static final int FIELD_NAME = 1001;
    }
}
