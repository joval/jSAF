// Copyright (C) 2011 jOVAL.org.  All rights reserved.

package jsaf.intf.windows.identity;

import jsaf.intf.identity.ICredential;

/**
 * A representation of a Windows credential.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IWindowsCredential extends ICredential {
    String getDomain();

    /**
     * Return a username of the form domain\name.
     */
    String getDomainUser();
}
