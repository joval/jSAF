// Copyright (C) 2011-2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;

import jsaf.identity.IdentityException;

/**
 * The IGroup interface provides information about a Windows group.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IGroup extends IPrincipal {
    /**
     * Get all the IPrincipals (users and groups) that are direct members of this group (non-recursive).
     *
     * @since 1.4
     */
    Collection<IPrincipal> members() throws IdentityException;
}
