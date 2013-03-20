// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import jsaf.Message;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.util.Base64;
import jsaf.util.StringTools;

/**
 * The ServiceDirectory class provides a mechanism to handle NT service SID mappings.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class ServiceDirectory {
    private static final String WQL = "select name, startname from win32_service";

    private IWindowsSession session;
    private IRunspace runspace;
    private Map<String, IUser> usersBySid, usersByName;
    private boolean initialized = false;

    ServiceDirectory(IWindowsSession session) throws Exception {
	this.session = session;
	usersByName = new HashMap<String, IUser>();
	usersBySid = new HashMap<String, IUser>();
	for (IRunspace runspace : session.getRunspacePool().enumerate()) {
	    if (runspace.getView() == session.getNativeView()) {
		this.runspace = runspace;
		break;
	    }
	}
	if (runspace == null) {
	    runspace = session.getRunspacePool().spawn(session.getNativeView());
	}
    }

    boolean isServiceSid(String sid) {
	return sid.startsWith("S-1-5-80-");
    }

    IUser queryUserBySid(String sid) throws NoSuchElementException {
	if (isServiceSid(sid)) {
	    load();
	    if (usersBySid.containsKey(sid)) {
		return usersBySid.get(sid);
	    }
	}
	throw new NoSuchElementException(sid);
    }

    IUser queryUser(String netbiosName) throws NoSuchElementException {
	String domain, name;
	int ptr = netbiosName.indexOf("\\");
	if (ptr == -1) {
	    domain = "NT SERVICE";
	    name = netbiosName;
	} else {
	    domain = netbiosName.substring(0,ptr).toUpperCase();
	    name = netbiosName.substring(ptr+1);
	}
	load();
	if ("NT SERVICE".equals(domain) && usersByName.containsKey(name.toLowerCase())) {
	    return usersByName.get(name.toLowerCase());
	} else {
	    throw new NoSuchElementException(netbiosName);
	}
    }

    /**
     * Returns a Collection of all the service users.
     */
    Collection<IUser> queryAllUsers() {
	load();
	return usersByName.values();
    }

    // Private

    /**
     * Idempotent
     */
    private void load() {
	if (initialized) return;
	try {
	    runspace.loadModule(getClass().getResourceAsStream("Service.psm1"));
	    String name=null, sid=null;
	    String data = new String(Base64.decode(runspace.invoke("Get-ServiceSids | Transfer-Encode")), StringTools.UTF8);
	    for (String line : data.split("\r\n")) {
		if (line.startsWith("NAME:")) {
		    name = line.substring(5).trim();
		} else if (line.startsWith("SERVICE SID:")) {
		    sid = line.substring(12).trim();
		} else if (line.startsWith("ACCOUNT:")) {
		    String account = line.substring(8).trim();
		    if (name != null && sid != null) {
			IUser user = new User("NT SERVICE", name, sid, new ArrayList<String>(), true);
			usersBySid.put(sid, user);
			usersByName.put(name.toLowerCase(), user);
		    }
		    name = null;
		    sid = null;
		}
	    }
	    initialized = true;
	} catch (Exception e) {
	    session.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	}
    }
}
