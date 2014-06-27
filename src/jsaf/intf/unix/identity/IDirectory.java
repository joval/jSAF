// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.identity;

import java.math.BigInteger;
import java.util.Collection;
import java.util.NoSuchElementException;

import jsaf.identity.IdentityException;

/**
 * Representation of a Unix user/group store.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3
 */
public interface IDirectory {
    /**
     * Returns a user object with the specified ID. There may not actually be a corresponding user defined on the
     * system.
     *
     * @since 1.3
     */
    public IUser getUser(BigInteger id);

    /**
     * Get a user by name.
     *
     * @throws NoSuchElementException if no such user is defined on the system.
     *
     * @since 1.3
     */
    public IUser lookupUser(String name) throws NoSuchElementException, IdentityException;

    /**
     * Returns a Collection of all the users defined on the system.
     *
     * @since 1.3
     */
    public Collection<IUser> listUsers() throws IdentityException;

    /**
     * Returns a group object with the specified ID. There may not actually be a corresponding group defined on the
     * system.
     *
     * @since 1.3
     */
    public IGroup getGroup(BigInteger id);

    /**
     * Get a group by name.
     *
     * @throws NoSuchElementException if no such group is defined on the system.
     *
     * @since 1.3
     */
    public IGroup lookupGroup(String name) throws NoSuchElementException, IdentityException;

    /**
     * Returns a Collection of all the groups defined on the system.
     *
     * @since 1.3
     */
    public Collection<IGroup> listGroups() throws IdentityException;
}
