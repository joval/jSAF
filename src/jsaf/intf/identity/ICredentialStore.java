// Copyright (C) 2011 jOVAL.org.  All rights reserved.

package jsaf.intf.identity;

import java.security.AccessControlException;

import jsaf.intf.system.IBaseSession;

/**
 * An interface for a credential storage mechanism.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ICredentialStore {
    /**
     * Return the appropriate credential for the specified IBaseSession.
     *
     * @return null if no credential is found
     *
     * @throws AccessControlException if access to the requested credential is not allowed.
     */
    ICredential getCredential(IBaseSession session) throws AccessControlException;
}
