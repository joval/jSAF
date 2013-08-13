// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.identity.IdentityException;
import jsaf.intf.windows.identity.IACE;
import jsaf.intf.windows.identity.IDirectory;
import jsaf.intf.windows.identity.IGroup;
import jsaf.intf.windows.identity.IPrincipal;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.io.LittleEndian;

/**
 * Implementation of IDirectory.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Directory implements IDirectory {
    /**
     * Get the Name portion of a DOMAIN\NAME String.  If there is no domain portion, returns the original String.
     */
    public static final String getName(String s) {
	int ptr = s.indexOf("\\");
	if (ptr == -1) {
	    return s;
	} else {
	    return s.substring(ptr+1);
	}
    }

    /**
     * Convert a hexidecimal String representation of a SID into a "readable" SID String.
     *
     * The WMI implementations return this kind of String when a binary is fetched using getAsString.
     * @see org.joval.intf.windows.wmi.ISWbemProperty
     */
    public static final String toSid(String hex) {
	int len = hex.length();
	if (len % 2 == 1) {
	    throw new IllegalArgumentException(hex);
	}

	byte[] raw = new byte[len/2];
	int index = 0;
	for (int i=0; i < len; i+=2) {
	    String s = hex.substring(i, i+2);
	    raw[index++] = (byte)(Integer.parseInt(s, 16) & 0xFF);
	}

	return toSid(raw);
    }

    /**
     * Convert a byte[] representation of a SID into a "readable" SID String.
     */
    public static final String toSid(byte[] raw) {
	int rev = raw[0];
	int subauthCount = raw[1];

	StringBuffer sb = new StringBuffer();
	for (int i=2; i < 8; i++) {
	    sb.append(LittleEndian.toHexString(raw[i]));
	}
	String idAuthStr = sb.toString();
	long idAuth = Long.parseLong(idAuthStr, 16);

	StringBuffer sid = new StringBuffer("S-");
	sid.append(Integer.toHexString(rev));
	sid.append("-");
	sid.append(Long.toHexString(idAuth));

	for (int i=0; i < subauthCount; i++) {
	    sid.append("-");
	    byte[] buff = new byte[4];
	    int base = 8 + i*4;
	    buff[0] = raw[base];
	    buff[1] = raw[base + 1];
	    buff[2] = raw[base + 2];
	    buff[3] = raw[base + 3];
	    sid.append(Long.toString(LittleEndian.getUInt(buff) & 0xFFFFFFFFL));
	}
	return sid.toString();
    }

    private IWindowsSession session;
    private LocLogger logger;
    private ActiveDirectory ad;
    private LocalDirectory local;
    private ServiceDirectory service;

    /**
     * Create a Directory with an AD interface based on the WMI provider.
     */
    public Directory(IWindowsSession session) throws Exception {
	this(session, new ActiveDirectory(session.getWmiProvider(), session.getLogger()));
    }

    /**
     * Create a Directory with the specified AD interface.
     */
    public Directory(IWindowsSession session, ActiveDirectory ad) throws Exception {
	this.session = session;
	this.ad = ad;
	logger = session.getLogger();
	local = new LocalDirectory(this, session.getWmiProvider());
	service = new ServiceDirectory(session);
    }

    /**
     * Returns whether the user is enabled. Used by "thin" local user objects.
     */
    boolean userEnabled(String sid) throws NoSuchElementException, IdentityException {
	if (service.isServiceSid(sid)) {
	    return true;
	} else {
	    try {
		return local.userEnabled(sid);
	    } catch (NoSuchElementException e) {
		return ad.queryUserBySid(sid).isEnabled();
	    }
	}
    }

    /**
     * Returns the collection of NETBIOS names of groups of which the user is a member. Used by "thin" local
     * user objects.
     */
    Collection<String> resolveUserGroupNames(String sid) throws NoSuchElementException, IdentityException {
	if (service.isServiceSid(sid)) {
	    return new ArrayList<String>();
	} else {
	    try {
		return local.resolveUserGroupNames(sid);
	    } catch (NoSuchElementException e) {
		return ad.queryUserBySid(sid).getGroupNetbiosNames();
	    }
	}
    }

    // Implement ILoggable

    public LocLogger getLogger() {
	return logger;
    }

    public void setLogger(LocLogger logger) {
	this.logger = logger;
	ad.setLogger(logger);
	local.setLogger(logger);
    }

    // Implement IDirectory

    public String getMachineName() {
	return session.getMachineName();
    }

    public IUser queryUserBySid(String sid) throws NoSuchElementException, IdentityException {
	if (service.isServiceSid(sid)) {
	    return service.queryUserBySid(sid);
	} else {
	    try {
		return local.queryUserBySid(sid);
	    } catch (NoSuchElementException e) {
		return ad.queryUserBySid(sid);
	    }
	}
    }

    public IUser queryUser(String netbiosName) throws IllegalArgumentException, NoSuchElementException, IdentityException {
	if (netbiosName.toUpperCase().startsWith("NT SERVICE\\")) {
	    return service.queryUser(netbiosName);
	} else if (isLocal(netbiosName)) {
	    return local.queryUser(netbiosName);
	} else {
	    return ad.queryUser(netbiosName);
	}
    }

    public Collection<IUser> queryAllUsers() throws IdentityException {
	return local.queryAllUsers();
    }

    public IGroup queryGroupBySid(String sid) throws NoSuchElementException, IdentityException {
	try {
	    return local.queryGroupBySid(sid);
	} catch (NoSuchElementException e) {
	    return ad.queryGroupBySid(sid);
	}
    }

    public IGroup queryGroup(String netbiosName) throws IllegalArgumentException, NoSuchElementException, IdentityException {
	if (isLocal(netbiosName)) {
	    return local.queryGroup(netbiosName);
	} else {
	    return ad.queryGroup(netbiosName);
	}
    }

    public Collection<IGroup> queryAllGroups() throws IdentityException {
	return local.queryAllGroups();
    }

    public IPrincipal queryPrincipal(String netbiosName)
		throws NoSuchElementException, IllegalArgumentException, IdentityException {

	if (netbiosName.toUpperCase().startsWith("NT SERVICE\\")) {
	    return service.queryUser(netbiosName);
	} else if (isLocal(netbiosName)) {
	    return local.queryPrincipal(netbiosName);
	} else {
	    return ad.queryPrincipal(netbiosName);
	}
    }

    public IPrincipal queryPrincipalBySid(String sid) throws NoSuchElementException, IdentityException {
	if (service.isServiceSid(sid)) {
	    return service.queryUserBySid(sid);
	} else {
	    try {
		return local.queryPrincipalBySid(sid);
	    } catch (NoSuchElementException e) {
		return ad.queryPrincipalBySid(sid);
	    }
	}
    }

    public Collection<IPrincipal> queryAllPrincipals() throws IdentityException {
	return local.queryAllPrincipals();
    }

    public boolean isLocal(String netbiosName) {
	return local.isMember(netbiosName);
    }

    public boolean isLocalSid(String sid) {
	return local.isMemberSid(sid);
    }

    public boolean isDomainMember(String netbiosName) {
	return ad.isMember(netbiosName);
    }

    public boolean isDomainSid(String sid) {
	return ad.isMemberSid(sid);
    }

    public String getQualifiedNetbiosName(String netbiosName) {
	return local.getQualifiedNetbiosName(netbiosName);
    }

    public Collection<IPrincipal> getAllPrincipals(IPrincipal principal, boolean includeGroups, boolean resolveGroups)
		throws IdentityException {

	//
	// Resolve group members if resolveGroups == true
	//
	Collection<IPrincipal> principals = new ArrayList<IPrincipal>();
	if (resolveGroups) {
	    Map<String, IPrincipal> map = new HashMap<String, IPrincipal>();
	    queryAllMembers(principal, map);
	    principals = map.values();
	} else {
	    principals.add(principal);
	}

	//
	// Filter out group-type IPrincipals if includeGroups == false
	//
	Collection<IPrincipal> results = new ArrayList<IPrincipal>();
	for (IPrincipal p : principals) {
	    switch(p.getType()) {
	      case GROUP:
		if (includeGroups) {
		    results.add(p);
		}
		break;
	      case USER:
		results.add(p);
		break;
	    }
	}
	return results;
    }

    // Private

    /**
     * Won't get stuck in a loop because it adds the groups themselves to the Map as it goes.
     */
    private void queryAllMembers(IPrincipal principal, Map<String, IPrincipal> principals) throws IdentityException {
	if (!principals.containsKey(principal.getSid())) {
	    principals.put(principal.getSid(), principal);
	    switch(principal.getType()) {
	      case GROUP:
		IGroup g = (IGroup)principal;
		//
		// Add users
		//
		for (String netbiosName : g.getMemberUserNetbiosNames()) {
		    try {
			queryAllMembers(queryUser(netbiosName), principals);
		    } catch (IllegalArgumentException e) {
			logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		    } catch (NoSuchElementException e) {
			logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		    }
		}
		//
		// Add subgroups
		//
		for (String netbiosName : g.getMemberGroupNetbiosNames()) {
		    try {
			queryAllMembers(queryGroup(netbiosName), principals);
		    } catch (IllegalArgumentException e) {
			logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		    } catch (NoSuchElementException e) {
			logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		    }
		}
		break;
	    }
	}
    }
}
