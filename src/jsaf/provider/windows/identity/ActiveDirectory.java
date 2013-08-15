// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.Collection;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.identity.IdentityException;
import jsaf.io.LittleEndian;
import jsaf.intf.util.ILoggable;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.intf.windows.wmi.ISWbemObject;
import jsaf.intf.windows.wmi.ISWbemObjectSet;
import jsaf.intf.windows.wmi.ISWbemPropertySet;
import jsaf.provider.windows.wmi.WmiException;
import jsaf.util.StringTools;

/**
 * The ActiveDirectory class provides a mechanism to query a Windows Active Directory through the WMI provider of a machine.
 * It is case-insensitive, and it intelligently caches results so that subsequent requests for the same object can be returned
 * from memory.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class ActiveDirectory implements ILoggable {
    private static final String DOMAIN_WQL = "SELECT Name, DomainName, DnsForestName FROM Win32_NTDomain";

    private static final String AD_NAMESPACE = "root\\directory\\ldap";
    private static final String COMMON_SELECT = "SELECT DS_sAMAccountName, DS_distinguishedName, DS_objectSid";
    private static final String USER_WQL = COMMON_SELECT +
	", DS_userPrincipalName, DS_memberOf, DS_userAccountControl FROM DS_User";
    private static final String USER_WQL_UPN_CONDITION = "DS_userPrincipalName='$upn'";
    private static final String GROUP_WQL = COMMON_SELECT + ", DS_member FROM DS_Group";
    private static final String GROUP_WQL_NAME_CONDITION = "DS_sAMAccountName='$name'";
    private static final String GROUP_WQL_DN_CONDITION = "DS_distinguishedName='$dn'";
    private static final String MEMBER_CONDITION = "DS_memberOf='$dn'";
    private static final String SID_CONDITION = "DS_objectSid='$sid'";

    private IWmiProvider wmi;

    protected LocLogger logger;
    protected Hashtable<String, User> usersByUpn;
    protected Hashtable<String, User> usersBySid;
    protected Hashtable<String, Group> groupsByNetbiosName;
    protected Hashtable<String, Group> groupsBySid;
    protected Hashtable<String, String> domains;
    protected boolean initialized = false;

    ActiveDirectory(IWmiProvider wmi, LocLogger logger) {
	this.wmi = wmi;
	this.logger = logger;
	domains = new Hashtable<String, String>();
	usersByUpn = new Hashtable<String, User>();
	usersBySid = new Hashtable<String, User>();
	groupsByNetbiosName = new Hashtable<String, Group>();
	groupsBySid = new Hashtable<String, Group>();
    }

    User queryUserBySid(String sid) throws NoSuchElementException, IdentityException {
	User user = usersBySid.get(sid);
	if (user == null) {
	    try {
		StringBuffer wql = new StringBuffer(USER_WQL);
		wql.append(" WHERE ");
		wql.append(SID_CONDITION.replaceAll("(?i)\\$sid", Matcher.quoteReplacement(sid)));
		ISWbemObjectSet os = wmi.execQuery(AD_NAMESPACE, wql.toString());
		if (os == null || os.getSize() == 0) {
		    throw new NoSuchElementException(sid);
		} else {
		    ISWbemPropertySet props = os.iterator().next().getProperties();
		    String upn = props.getItem("DS_userPrincipalName").getValueAsString();
		    String dn = props.getItem("DS_distinguishedName").getValueAsString();
		    String netbiosName = toNetbiosName(dn);
		    String domain = getDomain(netbiosName);
		    String name = Directory.getName(netbiosName);
		    Collection<String> groupNetbiosNames = parseGroups(props.getItem("DS_memberOf").getValueAsArray());
		    int uac = props.getItem("DS_userAccountControl").getValueAsInteger().intValue();
		    boolean enabled = 0x00000002 != (uac & 0x00000002); //0x02 flag indicates disabled
		    user = new User(domain, name, sid, groupNetbiosNames, enabled);
		    usersByUpn.put(upn.toUpperCase(), user);
		    usersBySid.put(sid, user);
		}
	    } catch (WmiException e) {
		throw new IdentityException(e);
	    }
	}
	return user;
    }

    User queryUser(String netbiosName) throws NoSuchElementException, IllegalArgumentException, IdentityException {
	String upn = toUserPrincipalName(netbiosName);
	User user = usersByUpn.get(upn.toUpperCase());
	if (user == null) {
	    try {
		StringBuffer wql = new StringBuffer(USER_WQL);
		wql.append(" WHERE ");
		wql.append(USER_WQL_UPN_CONDITION.replaceAll("(?i)\\$upn", Matcher.quoteReplacement(upn)));
		ISWbemObjectSet os = wmi.execQuery(AD_NAMESPACE, wql.toString());
		if (os == null || os.getSize() == 0) {
		    throw new NoSuchElementException(netbiosName);
		} else {
		    ISWbemPropertySet props = os.iterator().next().getProperties();
		    String name = Directory.getName(netbiosName);
		    String domain = getDomain(netbiosName);
		    String sid = Directory.toSid(props.getItem("DS_objectSid").getValueAsString());
		    Collection<String> groupNetbiosNames = parseGroups(props.getItem("DS_memberOf").getValueAsArray());
		    int uac = props.getItem("DS_userAccountControl").getValueAsInteger().intValue();
		    boolean enabled = 0x00000002 != (uac & 0x00000002); //0x02 flag indicates disabled
		    user = new User(domain, name, sid, groupNetbiosNames, enabled);
		    usersByUpn.put(upn.toUpperCase(), user);
		    usersBySid.put(sid, user);
		}
	    } catch (WmiException e) {
		throw new IdentityException(e);
	    }
	}
	return user;
    }

    Group queryGroupBySid(String sid) throws NoSuchElementException, IdentityException {
	Group group = groupsBySid.get(sid);
	if (group == null) {
	    try {
		StringBuffer wql = new StringBuffer(GROUP_WQL);
		wql.append(" WHERE ");
		wql.append(SID_CONDITION.replaceAll("(?i)\\$sid", Matcher.quoteReplacement(sid)));
		ISWbemObjectSet rows = wmi.execQuery(AD_NAMESPACE, wql.toString());
		if (rows == null || rows.getSize() == 0) {
		    throw new NoSuchElementException(sid);
		} else {
		    ISWbemPropertySet columns = rows.iterator().next().getProperties();
		    String name = columns.getItem("DS_sAMAccountName").getValueAsString();
		    String dn = columns.getItem("DS_distinguishedName").getValueAsString();
		    String netbiosName = toNetbiosName(dn);
		    String domain = getDomain(netbiosName);
		    Collection<String> userNetbiosNames = new ArrayList<String>();
		    Collection<String> groupNetbiosNames = new ArrayList<String>();
		    getMembers(dn, userNetbiosNames, groupNetbiosNames);
		    group = new Group(domain, name, sid, userNetbiosNames, groupNetbiosNames);
		    groupsByNetbiosName.put(netbiosName.toUpperCase(), group);
		    groupsBySid.put(sid, group);
		}
	    } catch (WmiException e) {
		throw new IdentityException(e);
	    }
	}
	return group;
    }

    Group queryGroup(String netbiosName) throws NoSuchElementException, IllegalArgumentException, IdentityException {
	Group group = groupsByNetbiosName.get(netbiosName.toUpperCase());
	if (group == null) {
	    if (isMember(netbiosName)) {
		String domain = getDomain(netbiosName);
		String dc = toDCString(domains.get(domain.toUpperCase()));
		String name = Directory.getName(netbiosName);
		try {
		    StringBuffer wql = new StringBuffer(GROUP_WQL);
		    wql.append(" WHERE ");
		    wql.append(GROUP_WQL_NAME_CONDITION.replaceAll("(?i)\\$name", Matcher.quoteReplacement(name)));
		    ISWbemObjectSet rows = wmi.execQuery(AD_NAMESPACE, wql.toString());
		    if (rows == null || rows.getSize() == 0) {
			throw new NoSuchElementException(netbiosName);
		    } else {
			for (ISWbemObject row : rows) {
			    ISWbemPropertySet columns = row.getProperties();
			    String dn = columns.getItem("DS_distinguishedName").getValueAsString();
			    if (dn.endsWith(dc)) {
				Collection<String> userNetbiosNames = new ArrayList<String>();
				Collection<String> groupNetbiosNames = new ArrayList<String>();
				getMembers(dn, userNetbiosNames, groupNetbiosNames);
				String sid = Directory.toSid(columns.getItem("DS_objectSid").getValueAsString());
				group = new Group(domain, name, sid, userNetbiosNames, groupNetbiosNames);
				groupsByNetbiosName.put(netbiosName.toUpperCase(), group);
				groupsBySid.put(sid, group);
				break;
			    } else {
				logger.trace(Message.STATUS_AD_GROUP_SKIP, dn, dc);
			    }
			}
		    }
		} catch (WmiException e) {
		    throw new IdentityException(e);
		}
		if (group == null) {
		    throw new NoSuchElementException(netbiosName);
		}
	    } else {
		throw new IllegalArgumentException(Message.getMessage(Message.ERROR_AD_DOMAIN_UNKNOWN, netbiosName));
	    }
	}
	return group;
    }

    Principal queryPrincipal(String netbiosName)
		throws NoSuchElementException, IllegalArgumentException, IdentityException {

	try {
	    return queryUser(netbiosName);
	} catch (NoSuchElementException e) {
	}
	return queryGroup(netbiosName);
    }

    Principal queryPrincipalBySid(String sid) throws NoSuchElementException, IdentityException {
	try {
	    return queryUserBySid(sid);
	} catch (NoSuchElementException e) {
	}
	return queryGroupBySid(sid);
    }

    boolean isMember(String netbiosName) throws IllegalArgumentException {
	initDomains();
	return domains.containsKey(getDomain(netbiosName));
    }

    boolean isMemberSid(String sid) {
	try {
	    queryPrincipalBySid(sid);
	    return true;
	} catch (NoSuchElementException e) {
	} catch (IdentityException e) {
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	}
	return false;
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Protected

    /**
     * Initialize the domain map, which is used to correlate domain names with their corresponding DNS names.
     */
    void initDomains() {
	if (initialized) {
	    return;
	}
	try {
	    for (ISWbemObject row : wmi.execQuery(IWmiProvider.CIMv2, DOMAIN_WQL)) {
		ISWbemPropertySet columns = row.getProperties();
		String domain = columns.getItem("DomainName").getValueAsString();
		String dns = columns.getItem("DnsForestName").getValueAsString();
		String name = columns.getItem("Name").getValueAsString();
		if (domain == null || dns == null) {
		    logger.trace(Message.STATUS_AD_DOMAIN_SKIP, name);
		} else {
		    logger.trace(Message.STATUS_AD_DOMAIN_ADD, domain, dns);
		    domains.put(domain.toUpperCase(), dns);
		}
	    }
	    initialized = true;
	} catch (WmiException e) {
	    logger.warn(Message.ERROR_AD_INIT);
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	}
    }

    /**
     * Given a name of the form DOMAIN\\username, return the corresponding UserPrincipalName of the form username@domain.com.
     */
    String toUserPrincipalName(String netbiosName) throws IllegalArgumentException {
	String domain = getDomain(netbiosName);
	if (isMember(netbiosName)) {
	    String dns = domains.get(domain.toUpperCase());
	    String upn = new StringBuffer(Directory.getName(netbiosName)).append("@").append(dns).toString();
	    logger.trace(Message.STATUS_UPN_CONVERT, netbiosName, upn);
	    return upn;
	} else {
	    throw new IllegalArgumentException(Message.getMessage(Message.ERROR_AD_DOMAIN_UNKNOWN, netbiosName));
	}
    }

    /**
     * Get the DNS name of the DN's domain.
     */
    String getDNS(String dn) {
	StringBuffer dns = new StringBuffer();
	String remainder = dn;
	int ptr = dn.indexOf(",DC=");
	if (ptr == -1) return null;
	while (ptr != -1) {
	    int next = dn.indexOf(",DC=", ptr+4);
	    if (dns.length() > 0) {
		dns.append(".");
	    }
	    if (next == -1) {
		dns.append(dn.substring(ptr+4));
	    } else {
		dns.append(dn.substring(ptr+4, next));
	    }
	    ptr = next;
	}
	return dns.toString();
    }

    /**
     * Convert a DNS path into a Netbios domain name.
     */
    String dnsToDomain(String dns) {
	for (String domain : domains.keySet()) {
	    if (dns.equals(domains.get(domain))) {
		return domain;
	    }
	}
	return null;
    }

    /**
     * Convert a String of the form a.b.com to a String of the form DC=a,DC=b,DC=com.
     */
    String toDCString(String dns) {
	StringBuffer sb = new StringBuffer();
	for (String token : StringTools.toList(StringTools.tokenize(dns, "."))) {
	    if (sb.length() > 0) {
		sb.append(",");
	    }
	    sb.append("DC=");
	    sb.append(token);
	}
	return sb.toString();
    }

    /**
     * Get the Domain portion of a Domain\\Name String.
     */
    String getDomain(String netbiosName) throws IllegalArgumentException {
	int ptr = netbiosName.indexOf("\\");
	if (ptr == -1) {
	    throw new IllegalArgumentException(Message.getMessage(Message.ERROR_AD_DOMAIN_REQUIRED, netbiosName));
	} else {
	    return netbiosName.substring(0, ptr);
	}
    }

    /**
     * Convert a DN to a Netbios Name.
     *
     * @throws NoSuchElementException if the domain can not be found
     */
    String toNetbiosName(String dn) throws NoSuchElementException {
	int ptr = dn.indexOf(",");
	String groupName = dn.substring(3, ptr); // Starts with CN=
	ptr = dn.indexOf(",DC=");
	StringBuffer dns = new StringBuffer();
	for (String name : StringTools.toList(StringTools.tokenize(dn.substring(ptr), ",DC="))) {
	    if (dns.length() > 0) {
		dns.append(".");
	    }
	    dns.append(name);
	}
	String domain = dnsToDomain(dns.toString());
	if (domain == null) {
	    throw new NoSuchElementException(Message.getMessage(Message.STATUS_NAME_DOMAIN_ERR, dn));
	}
	String name = domain + "\\" + groupName;
	logger.trace(Message.STATUS_NAME_DOMAIN_OK, dn, name);
	return name;
    }

    // Private

    /**
     * Convert a String[] of group DNs into a Collection of DOMAIN\\group names.
     */
    Collection<String> parseGroups(String[] dns) throws IdentityException {
	try {
	    Collection<String> groups = new ArrayList<String>(dns.length);
	    for (String groupDN : dns) {
		StringBuffer wql = new StringBuffer(GROUP_WQL);
		wql.append(" WHERE ");
		wql.append(GROUP_WQL_DN_CONDITION.replaceAll("(?i)\\$dn", Matcher.quoteReplacement(groupDN)));
		ISWbemObjectSet rows = wmi.execQuery(AD_NAMESPACE, wql.toString());
		if (rows == null || rows.getSize() == 0) {
		    throw new NoSuchElementException(groupDN);
		} else {
		    ISWbemPropertySet columns = rows.iterator().next().getProperties();
		    String name = columns.getItem("DS_sAMAccountName").getValueAsString();
		    String domain = dnsToDomain(getDNS(columns.getItem("DS_distinguishedName").getValueAsString()));
		    groups.add(domain + "\\" + name);
		}
	    }
	    return groups;
	} catch (WmiException e) {
	    throw new IdentityException(e);
	}
    }

    /**
     * Given the DN of a group, sort its user and subgroup members into different collections.
     */
    private void getMembers(String groupDN, Collection<String> users, Collection<String> groups) throws IdentityException {
	try {
	    StringBuffer wql = new StringBuffer(USER_WQL);
	    wql.append(" WHERE ");
	    wql.append(MEMBER_CONDITION.replaceAll("(?i)\\$dn", Matcher.quoteReplacement(groupDN)));
	    for (ISWbemObject row : wmi.execQuery(AD_NAMESPACE, wql.toString())) {
		ISWbemPropertySet props = row.getProperties();
		String name = props.getItem("DS_sAMAccountName").getValueAsString();
		users.add(toNetbiosName("CN=" + name + "," + props.getItem("DS_distinguishedName").getValueAsString()));
	    }
	    wql = new StringBuffer(GROUP_WQL);
	    wql.append(" WHERE ");
	    wql.append(MEMBER_CONDITION.replaceAll("(?i)\\$dn", Matcher.quoteReplacement(groupDN)));
	    for (ISWbemObject row : wmi.execQuery(AD_NAMESPACE, wql.toString())) {
		ISWbemPropertySet props = row.getProperties();
		String name = props.getItem("DS_sAMAccountName").getValueAsString();
		groups.add(toNetbiosName("CN=" + name + "," + props.getItem("DS_distinguishedName").getValueAsString()));
	    }
	} catch (WmiException e) {
	    throw new IdentityException(e);
	}
    }
}
