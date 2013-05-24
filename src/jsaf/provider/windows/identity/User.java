// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.Collection;

import jsaf.intf.windows.identity.IDirectory;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.system.IWindowsSession;

/**
 * The User class stores information about a Windows user.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class User extends Principal implements IUser {
    private Boolean enabled = null;
    private Collection<String> groupNetbiosNames = null;

    private Directory directory = null;

    /**
     * Create a user whose group memberships and enabled status will be determined if and when queried.
     */
    public User(IWindowsSession session, String accountName, String sid) {
	super(session, accountName, sid);
	directory = (Directory)session.getDirectory();
    }

    User(String domain, String name, String sid, Collection<String> groupNetbiosNames, boolean enabled) {
	super(domain, name, sid);
	this.groupNetbiosNames = groupNetbiosNames;
	this.enabled = enabled ? Boolean.TRUE : Boolean.FALSE;
    }

    void setEnabled(boolean enabled) {
	if (enabled) {
	    this.enabled = Boolean.TRUE;
	} else {
	    this.enabled = Boolean.FALSE;
	}
    }

    // Implement IUser

    public Collection<String> getGroupNetbiosNames() {
	if (groupNetbiosNames == null) {
	    try {
		groupNetbiosNames = directory.resolveUserGroupNames(sid);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return groupNetbiosNames;
    }

    public boolean isEnabled() {
	if (enabled == null) {
	    try {
		enabled = directory.userEnabled(sid);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return enabled.booleanValue();
    }

    // Implement IPrincipal

    public Type getType() {
	return Type.USER;
    }
}
