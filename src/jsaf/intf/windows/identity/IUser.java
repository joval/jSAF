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
    public Collection<String> getGroupNetbiosNames();

    public boolean isEnabled();
}
