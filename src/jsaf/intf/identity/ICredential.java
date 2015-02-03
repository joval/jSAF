// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.identity;

/**
 * A representation of an abstract credential, consisting of a username and password.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ICredential {
    /**
     * Get the username.
     *
     * @since 1.0
     */
    String getUsername();

    /**
     * Get the password.
     *
     * @since 1.0.1
     */
    char[] getPassword();
}
