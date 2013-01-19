// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;

/**
 * The IUser interface provides information about a Windows user.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IUser extends IPrincipal {
    /**
     * Returns the Netbios names (DOMAIN\NAME) of all groups of which the user is a member.  Non-recursive (i.e., only
     * groups containing this user, not groups containing groups containing this user, etc.).
     */
    public Collection<String> getGroupNetbiosNames();

    /**
     * Is the user account enabled or disabled?
     */
    public boolean isEnabled();
}
