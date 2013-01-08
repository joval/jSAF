// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

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
