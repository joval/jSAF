// Copyright (C) 2011-2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;
import java.util.NoSuchElementException;

import jsaf.identity.IdentityException;
import jsaf.intf.util.ILoggable;

/**
 * Representation of a Windows user/group store.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IDirectory extends ILoggable {
    /**
     * Returns whether or not the argument matches a valid SID pattern.
     *
     * @since 1.4
     */
    public boolean isSid(String arg);

    /**
     * Returns the user corresponding to the specified SID.
     *
     * @since 1.0
     */
    public IUser queryUserBySid(String sid) throws NoSuchElementException, IdentityException;

    /**
     * Query for an individual user.  The input parameter should be of the form DOMAIN\NAME.  For built-in users, the
     * DOMAIN\ part can be dropped, in which case the name parameter is just the user name.
     *
     * @throws IllegalArgumentException if the domain is not recognized
     * @throws NoSuchElementException if the group does not exist
     *
     * @since 1.0
     */
    public IUser queryUser(String netbiosName) throws IllegalArgumentException, NoSuchElementException, IdentityException;

    /**
     * Returns a Collection of all the local users.
     *
     * @since 1.0
     */
    public Collection<IUser> queryAllUsers() throws IdentityException;

    /**
     * Returns the group corresponding to the specified SID.
     *
     * @since 1.0
     */
    public IGroup queryGroupBySid(String sid) throws NoSuchElementException, IdentityException;

    /**
     * Query for an individual group.  The input parameter should be of the form DOMAIN\NAME.  For built-in groups, the
     * DOMAIN\ part can be dropped, in which case the name parameter is just the group name.
     *
     * @throws IllegalArgumentException if the domain is not recognized
     * @throws NoSuchElementException if the group does not exist
     *
     * @since 1.0
     */
    public IGroup queryGroup(String netbiosName) throws IllegalArgumentException, NoSuchElementException, IdentityException;

    /**
     * Returns a Collection of all the local groups.
     *
     * @since 1.0
     */
    public Collection<IGroup> queryAllGroups() throws IdentityException;

    /**
     * Returns a Principal (User or Group) given a Netbios name.
     *
     * @throws IllegalArgumentException if the domain is not recognized
     * @throws NoSuchElementException if no matching user or group exists
     *
     * @since 1.0
     */
    public IPrincipal queryPrincipal(String netbiosName)
	throws IllegalArgumentException, NoSuchElementException, IdentityException;

    /**
     * Returns a Principal (User or Group) given a sid.
     *
     * @since 1.0
     */
    public IPrincipal queryPrincipalBySid(String sid) throws NoSuchElementException, IdentityException;

    /**
     * Returns a Collection of all local users and groups.
     *
     * @since 1.0
     */
    public Collection<IPrincipal> queryAllPrincipals() throws IdentityException;

    /**
     * Does the local machine recognize this principal?
     *
     * @since 1.0
     */
    public boolean isLocal(String netbiosName);

    /**
     * Does the local machine recognize this SID?
     *
     * @since 1.0
     */
    public boolean isLocalSid(String sid) throws IdentityException;

    /**
     * Returns the SID for the local machine.
     *
     * @since 1.3
     */
    public String getComputerSid() throws IdentityException;

    /**
     * Get the members of the principal, if it's a group.
     *
     * @param includeGroups set to true to include group principals in the result, false if you only want users
     * @param resolveGroups gets members recursively if true
     *
     * @since 1.0
     */
    public Collection<IPrincipal> getAllPrincipals(IPrincipal principal, boolean includeGroups, boolean resolveGroups)
	throws IdentityException;
}
