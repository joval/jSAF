// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Matcher;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.util.ILoggable;
import jsaf.intf.windows.identity.IGroup;
import jsaf.intf.windows.identity.IPrincipal;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.intf.windows.wmi.ISWbemObject;
import jsaf.intf.windows.wmi.ISWbemObjectSet;
import jsaf.intf.windows.wmi.ISWbemPropertySet;
import jsaf.provider.windows.wmi.WmiException;

/**
 * The LocalDirectory class provides a mechanism to query the local User/Group directory for a Windows machine using WMI.
 * It is case-insensitive, and it intelligently caches results so that subsequent requests for the same object can be returned
 * from memory.
 *
 * An alternative method to implement this functionality would be to use the NetUserEnum function. See this example:
 * http://stackoverflow.com/questions/12424418/getting-user-info-from-networkapi-user-info-3-user-info-4-structures
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class LocalDirectory implements ILoggable {
    static final String USER_WQL		= "SELECT SID, Name, Domain, Disabled FROM Win32_UserAccount";
    static final String SYSUSER_WQL		= "SELECT SID, Name, Domain FROM Win32_SystemAccount";
    static final String GROUP_WQL		= "SELECT SID, Name, Domain FROM Win32_Group";
    static final String LOCAL_CONDITION		= "LocalAccount=TRUE";
    static final String DOMAIN_CONDITION	= "Domain='$domain'";
    static final String NAME_CONDITION		= "Name='$name'";
    static final String SID_CONDITION		= "SID='$sid'";

    static final String USER_GROUP_WQL		= "SELECT * FROM Win32_GroupUser WHERE PartComponent=\"$conditions\"";
    static final String USER_DOMAIN_CONDITION	= "Win32_UserAccount.Domain='$domain'";

    static final String GROUP_USER_WQL		= "SELECT * FROM Win32_GroupUser WHERE GroupComponent=\"$conditions\"";
    static final String GROUP_DOMAIN_CONDITION	= "Win32_Group.Domain='$domain'";

    private Hashtable<String, IUser> usersBySid;
    private Hashtable<String, IUser> usersByNetbiosName;
    private Hashtable<String, IGroup> groupsBySid;
    private Hashtable<String, IGroup> groupsByNetbiosName;

    private String hostname;
    private Directory parent;
    private IWmiProvider wmi;
    private LocLogger logger;
    private boolean preloadedUsers = false;
    private boolean preloadedGroups = false;

    LocalDirectory(String hostname, Directory parent) {
	this.hostname = hostname;
	this.parent = parent;
	this.logger = parent.getLogger();
	usersByNetbiosName = new Hashtable<String, IUser>();
	usersBySid = new Hashtable<String, IUser>();
	groupsByNetbiosName = new Hashtable<String, IGroup>();
	groupsBySid = new Hashtable<String, IGroup>();
	wmi = parent.getSession().getWmiProvider();
    }

    IUser queryUserBySid(String sid) throws NoSuchElementException, WmiException {
	IUser user = usersBySid.get(sid);
	if (user == null) {
	    if (preloadedUsers) {
		throw new NoSuchElementException(sid);
	    }

	    StringBuffer conditions = new StringBuffer(" WHERE ");
	    conditions.append(SID_CONDITION.replaceAll("(?i)\\$sid", Matcher.quoteReplacement(sid)));
	    ISWbemObjectSet os = wmi.execQuery(IWmiProvider.CIMv2, USER_WQL + conditions.toString());
	    if (os.getSize() == 0) {
		os = wmi.execQuery(IWmiProvider.CIMv2, SYSUSER_WQL + conditions.toString());
	    }
	    if (os.getSize() == 0) {
		throw new NoSuchElementException(sid);
	    }
	    user = preloadUser(os.iterator().next().getProperties());
	}
	return user;
    }

    /**
     * Query for an individual user.  The input parameter should be of the form DOMAIN\\username.  For built-in accounts,
     * the DOMAIN\\ part can be dropped, in which case the input parameter can be just the username.
     *
     * @throws NoSuchElementException if the user does not exist
     */
    IUser queryUser(String netbiosName) throws NoSuchElementException, WmiException {
	String domain = getDomain(netbiosName);
	String name = parent.getName(netbiosName);
	netbiosName = domain + "\\" + name; // in case no domain was specified in the original netbiosName

	IUser user = usersByNetbiosName.get(netbiosName.toUpperCase());
	if (user == null) {
	    if (preloadedUsers) {
		throw new NoSuchElementException(netbiosName);
	    }

	    StringBuffer conditions = new StringBuffer(" WHERE ");
	    conditions.append(NAME_CONDITION.replaceAll("(?i)\\$name", Matcher.quoteReplacement(name)));
	    conditions.append(" AND ");
	    if (domain.equalsIgnoreCase(hostname)) {
		conditions.append(LOCAL_CONDITION);
	    } else {
		conditions.append(DOMAIN_CONDITION.replaceAll("(?i)\\$domain", Matcher.quoteReplacement(domain)));
	    }
	    ISWbemObjectSet os = wmi.execQuery(IWmiProvider.CIMv2, USER_WQL + conditions.toString());
	    if (os.getSize() == 0) {
		os = wmi.execQuery(IWmiProvider.CIMv2, SYSUSER_WQL + conditions.toString());
	    }
	    if (os.getSize() == 0) {
		throw new NoSuchElementException(netbiosName);
	    }
	    user = preloadUser(os.iterator().next().getProperties());
	}
	return user;
    }

    /**
     * Returns a Collection of all the local users.
     */
    Collection<IUser> queryAllUsers() throws WmiException {
	if (!preloadedUsers) {
	    StringBuffer conditions = new StringBuffer(" WHERE ");
	    conditions.append(LOCAL_CONDITION);
	    for (ISWbemObject row : wmi.execQuery(IWmiProvider.CIMv2, USER_WQL + conditions.toString())) {
		preloadUser(row.getProperties());
	    }
	    for (ISWbemObject row : wmi.execQuery(IWmiProvider.CIMv2, SYSUSER_WQL + conditions.toString())) {
		preloadUser(row.getProperties());
	    }
	    preloadedUsers = true;
	}
	return usersByNetbiosName.values();
    }

    IGroup queryGroupBySid(String sid) throws NoSuchElementException, WmiException {
	IGroup group = groupsBySid.get(sid);
	if (group == null) {
	    if (preloadedGroups) {
		throw new NoSuchElementException(sid);
	    }

	    StringBuffer wql = new StringBuffer(GROUP_WQL);
	    wql.append(" WHERE ");
	    wql.append(SID_CONDITION.replaceAll("(?i)\\$sid", Matcher.quoteReplacement(sid)));

	    ISWbemObjectSet os = wmi.execQuery(IWmiProvider.CIMv2, wql.toString());
	    if (os.getSize() == 0) {
		throw new NoSuchElementException(sid);
	    } else {
		ISWbemPropertySet columns = os.iterator().next().getProperties();
		String name = columns.getItem("Name").getValueAsString();
		String domain = columns.getItem("Domain").getValueAsString();
		group = makeGroup(domain, name, sid);
		groupsByNetbiosName.put((domain + "\\" + name).toUpperCase(), group);
		groupsBySid.put(sid, group);
	    }
	}
	return group;
    }

    /**
     * Query for an individual group.  The input parameter should be of the form DOMAIN\\name.  For built-in groups, the
     * DOMAIN\\ part can be dropped, in which case the name parameter is just the group name.
     *
     * @throws NoSuchElementException if the group does not exist
     */
    IGroup queryGroup(String netbiosName) throws NoSuchElementException, WmiException {
	String domain = getDomain(netbiosName);
	String name = parent.getName(netbiosName);
	netbiosName = domain + "\\" + name; // in case no domain was specified in the original netbiosName

	IGroup group = groupsByNetbiosName.get(netbiosName.toUpperCase());
	if (group == null) {
	    if (preloadedGroups) {
		throw new NoSuchElementException(netbiosName);
	    }

	    StringBuffer wql = new StringBuffer(GROUP_WQL);
	    wql.append(" WHERE ");
	    wql.append(NAME_CONDITION.replaceAll("(?i)\\$name", Matcher.quoteReplacement(name)));
	    wql.append(" AND ");
	    if (domain.equalsIgnoreCase(hostname)) {
		wql.append(LOCAL_CONDITION);
	    } else {
		wql.append(DOMAIN_CONDITION.replaceAll("(?i)\\$domain", Matcher.quoteReplacement(domain)));
	    }

	    ISWbemObjectSet os = wmi.execQuery(IWmiProvider.CIMv2, wql.toString());
	    if (os.getSize() == 0) {
		throw new NoSuchElementException(netbiosName);
	    } else {
		ISWbemPropertySet columns = os.iterator().next().getProperties();
		String sid = columns.getItem("SID").getValueAsString();
		group = makeGroup(domain, name, sid);
		groupsByNetbiosName.put(netbiosName.toUpperCase(), group);
		groupsBySid.put(sid, group);
	    }
	}
	return group;
    }

    /**
     * Returns a Collection of all the local groups.
     */
    Collection<IGroup> queryAllGroups() throws WmiException {
	if (!preloadedGroups) {
	    StringBuffer wql = new StringBuffer(GROUP_WQL);
	    wql.append(" WHERE ");
	    wql.append(LOCAL_CONDITION);
	    for (ISWbemObject rows : wmi.execQuery(IWmiProvider.CIMv2, wql.toString())) {
		ISWbemPropertySet columns = rows.getProperties();
		String domain = columns.getItem("Domain").getValueAsString();
		String name = columns.getItem("Name").getValueAsString();
		String netbiosName = domain + "\\" + name;
		String sid = columns.getItem("SID").getValueAsString();
		if (groupsByNetbiosName.get(netbiosName.toUpperCase()) == null) {
		    Group group = makeGroup(domain, name, sid);
		    groupsByNetbiosName.put(netbiosName.toUpperCase(), group);
		    groupsBySid.put(sid, group);
		}
	    }
	    preloadedGroups = true;
	}
	return groupsByNetbiosName.values();
    }

    /**
     * Returns a Principal (User or Group) given a Netbios name.
     */
    IPrincipal queryPrincipal(String netbiosName) throws NoSuchElementException, WmiException {
	try {
	    return queryUser(netbiosName);
	} catch (NoSuchElementException e) {
	}
	return queryGroup(netbiosName);
    }

    /**
     * Returns a Principal (User or Group) given a sid.
     */
    IPrincipal queryPrincipalBySid(String sid) throws NoSuchElementException, WmiException {
	try {
	    return queryUserBySid(sid);
	} catch (NoSuchElementException e) {
	}
	return queryGroupBySid(sid);
    }

    /**
     * Returns a Collection of all local users and groups.
     */
    Collection<IPrincipal> queryAllPrincipals() throws WmiException {
	Collection<IPrincipal> result = new Vector<IPrincipal>();
	result.addAll(queryAllUsers());
	result.addAll(queryAllGroups());
	return result;
    }

    /**
     * Returns whether or not the specified netbiosName is a member of this directory, meaning that the domain matches
     * the local hostname.
     */
    boolean isMember(String netbiosName) {
	String domain = getDomain(netbiosName);
	return hostname.equalsIgnoreCase(domain);
    }

    boolean isMemberSid(String sid) {
	try {
	    queryPrincipalBySid(sid);
	    return true;
	} catch (NoSuchElementException e) {
	} catch (WmiException e) {
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	}
	return false;
    }

    /**
     * Fills in the domain with the local hostname if it is not specified in the argument.
     */
    String getQualifiedNetbiosName(String netbiosName) {
	String domain = getDomain(netbiosName);
	if (domain == null) {
	    domain = hostname.toUpperCase();
	}
	return domain + "\\" + parent.getName(netbiosName);
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Private

    private IUser preloadUser(ISWbemPropertySet columns) throws WmiException {
	String domain = columns.getItem("Domain").getValueAsString();
	String name = columns.getItem("Name").getValueAsString();
	String netbiosName = domain + "\\" + name;
	String sid = columns.getItem("SID").getValueAsString();
	boolean enabled = true;
	if (columns.getItem("Disabled") != null) {
	    enabled = !columns.getItem("Disabled").getValueAsBoolean().booleanValue();
	}
	if (usersBySid.containsKey(sid)) {
	    return usersBySid.get(sid);
	}
	IUser user = makeUser(domain, name, sid, enabled);
	usersByNetbiosName.put(netbiosName.toUpperCase(), user);
	usersBySid.put(sid, user);
	return user;
    }

    /**
     * Get the Domain portion of a Domain\\Name String.  If Domain is not specified, this method returns the hostname
     * used in the LocalDirectory constructor.
     */
    private String getDomain(String s) {
	int ptr = s.indexOf("\\");
	if (ptr == -1) {
	    return hostname;
	} else {
	    return s.substring(0, ptr);
	}
    }

    private User makeUser(String domain, String name, String sid, boolean enabled) throws WmiException {
	StringBuffer conditions = new StringBuffer();
	conditions.append(USER_DOMAIN_CONDITION.replaceAll("(?i)\\$domain", Matcher.quoteReplacement(domain)));
	conditions.append(",");
	conditions.append(NAME_CONDITION.replaceAll("(?i)\\$name", Matcher.quoteReplacement(name)));
	String wql = USER_GROUP_WQL.replaceAll("(?i)\\$conditions", Matcher.quoteReplacement(conditions.toString()));

	Collection<String> groupNetbiosNames = new Vector<String>();
	for (ISWbemObject row : wmi.execQuery(IWmiProvider.CIMv2, wql)) {
	    ISWbemPropertySet columns = row.getProperties();
	    String groupComponent = columns.getItem("GroupComponent").getValueAsString();
	    int begin = groupComponent.indexOf("Domain=\"") + 8;
	    int end = groupComponent.indexOf("\"", begin);
	    String groupDomain = groupComponent.substring(begin, end);
	    begin = groupComponent.indexOf("Name=\"") + 6;
	    end = groupComponent.indexOf("\"", begin+1);
	    String groupName = groupComponent.substring(begin, end);
	    groupNetbiosNames.add(groupDomain + "\\" + groupName);
	}
	return new User(domain, name, sid, groupNetbiosNames, enabled);
    }

    private Group makeGroup(String domain, String name, String sid) throws WmiException {
	StringBuffer conditions = new StringBuffer();
	conditions.append(GROUP_DOMAIN_CONDITION.replaceAll("(?i)\\$domain", Matcher.quoteReplacement(domain)));
	conditions.append(",");
	conditions.append(NAME_CONDITION.replaceAll("(?i)\\$name", Matcher.quoteReplacement(name)));
	String wql = GROUP_USER_WQL.replaceAll("(?i)\\$conditions", Matcher.quoteReplacement(conditions.toString()));

	Collection<String> groupNetbiosNames = new Vector<String>(), userNetbiosNames = new Vector<String>();
	for (ISWbemObject row : wmi.execQuery(IWmiProvider.CIMv2, wql)) {
	    ISWbemPropertySet columns = row.getProperties();
	    String partComponent = columns.getItem("PartComponent").getValueAsString();
	    int begin = partComponent.indexOf("Domain=\"") + 8;
	    int end = partComponent.indexOf("\"", begin);
	    String memberDomain = partComponent.substring(begin, end);
	    begin = partComponent.indexOf("Name=\"") + 6;
	    end = partComponent.indexOf("\"", begin+1);
	    String memberName = partComponent.substring(begin, end);
	    if (partComponent.indexOf("Win32_UserAccount") != -1) {
		userNetbiosNames.add(memberDomain + "\\" + memberName);
	    } else if (partComponent.indexOf("Win32_Group") != -1) {
		groupNetbiosNames.add(memberDomain + "\\" + memberName);
	    }
	}
	return new Group(domain, name, sid, userNetbiosNames, groupNetbiosNames);
    }
}
