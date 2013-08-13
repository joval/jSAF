// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.util.NoSuchElementException;

import jsaf.identity.IdentityException;
import jsaf.intf.system.ISession;
import jsaf.intf.windows.identity.IDirectory;
import jsaf.intf.windows.identity.IGroup;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.system.IWindowsSession;

public class AD {
    IWindowsSession session;

    public AD(ISession session) {
	this.session = (IWindowsSession)session;
    }

    public void test(String name) {
	try {
	    IDirectory ad = session.getDirectory();
	    try {
		IUser user = ad.queryUser(name);
		System.out.println("User Name: " + name);
		System.out.println("SID: " + user.getSid());
		System.out.println("Enabled: " + user.isEnabled());
		for (String group : user.getGroupNetbiosNames()) {
		    System.out.println("Group: " + group);
		}
	    } catch (NoSuchElementException e) {
		System.out.println("User " + name + " not found.");
	    }
	    try {
		IGroup group = ad.queryGroup(name);
		System.out.println("Group Name: " + name);
		System.out.println("SID: " + group.getSid());
		for (String userMember : group.getMemberUserNetbiosNames()) {
		    System.out.println("Member User: " + userMember);
		}
		for (String groupMember : group.getMemberGroupNetbiosNames()) {
		    System.out.println("Member Group: " + groupMember);
		}
	    } catch (NoSuchElementException e) {
		System.out.println("Group " + name + " not found.");
	    }
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	} catch (IdentityException e) {
	    e.printStackTrace();
	}
    }
}

