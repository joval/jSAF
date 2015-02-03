// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;

/**
 * The IGroup interface provides information about a Windows group.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IGroup extends IPrincipal {
    /**
     * Get the Netbios names (DOMAIN\NAME) of users who are members of this group. Non-recursive (i.e., does not return
     * names of users who are members of groups that are members of this group).
     *
     * @since 1.0
     */
    Collection<String> getMemberUserNetbiosNames();

    /**
     * Get the Netbios names (DOMAIN\NAME) of groups which are members of this group. Non-recursive (i.e., does not return
     * names of groups which are members of groups which are members of this group).
     *
     * @since 1.0
     */
    Collection<String> getMemberGroupNetbiosNames();
}
