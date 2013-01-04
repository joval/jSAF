// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.Collection;

import jsaf.intf.windows.identity.IGroup;

/**
 * The Group class stores information about a Windows group.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class Group extends Principal implements IGroup {
    private Collection<String> memberUserNetbiosNames;
    private Collection<String> memberGroupNetbiosNames;

    Group(String domain, String name, String sid,
	  Collection<String> memberUserNetbiosNames, Collection<String> memberGroupNetbiosNames) {

	super(domain, name, sid);
	this.memberUserNetbiosNames = memberUserNetbiosNames;
	this.memberGroupNetbiosNames = memberGroupNetbiosNames;
    }

    // Implement IGroup

    public Collection<String> getMemberUserNetbiosNames() {
	return memberUserNetbiosNames;
    }

    public Collection<String> getMemberGroupNetbiosNames() {
	return memberGroupNetbiosNames;
    }

    // Implement IPrincipal

    public Type getType() {
	return Type.GROUP;
    }
}
