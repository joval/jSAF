// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.identity;

import java.math.BigInteger;

import jsaf.identity.IdentityException;

/**
 * The IGroup interface provides information about a Unix group.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3
 */
public interface IGroup {
    /**
     * Get the group's ID.
     *
     * @since 1.3
     */
    public BigInteger getId();

    /**
     * Get the group's name.
     *
     * @since 1.3
     */
    public String getName() throws IdentityException;
}
