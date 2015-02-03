// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.identity;

import java.security.AccessControlException;

import jsaf.intf.system.ISession;

/**
 * An interface for a credential storage mechanism.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ICredentialStore {
    /**
     * Return the appropriate credential for the given identifier.
     *
     * @return null if no credential is found
     *
     * @throws AccessControlException if access to the requested credential is not allowed.
     *
     * @since 1.1
     */
    ICredential getCredential(String identifier) throws AccessControlException;
}
