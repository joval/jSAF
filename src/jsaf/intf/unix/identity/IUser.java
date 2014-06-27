// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.identity;

import java.math.BigInteger;

import jsaf.identity.IdentityException;

/**
 * The IUser interface provides information about a Unix user.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3
 */
public interface IUser {
    /**
     * Get the user ID.
     *
     * @since 1.3
     */
    public BigInteger getId();

    /**
     * Get the username.
     *
     * @since 1.3
     */
    public String getName() throws IdentityException;
}
