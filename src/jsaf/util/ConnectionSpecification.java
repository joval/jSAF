// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.net.InetSocketAddress;
import java.net.Proxy;

import jsaf.intf.identity.ICredential;
import jsaf.intf.identity.ICredentialStore;
import jsaf.intf.util.IConnectionSpecification;

/**
 * An implementation of IConnectionSpecification.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0.2
 */
public class ConnectionSpecification implements IConnectionSpecification {
    private static ICredentialStore CREDENTIALS;

    public static void setCredentialStore(ICredentialStore cs) {
	CREDENTIALS = cs;
    }

    private ICredentialStore cs;

    protected int port = -1;
    protected Type type;
    protected String id;
    protected String hostname;
    protected IConnectionSpecification gateway;

    public ConnectionSpecification(String hostname) {
	this.hostname = hostname;
	this.id = hostname;
	type = Type.UNKNOWN;
    }

    public ConnectionSpecification(String hostname, IConnectionSpecification gateway) {
	this(hostname);
	if (gateway == null) {
	    id = hostname;
	} else {
	    this.gateway = gateway;
	    id = new StringBuffer(hostname).append("-via-").append(gateway.getIdentifier()).toString();
	}
    }

    public ConnectionSpecification(Proxy proxy) throws IllegalArgumentException {
	if (proxy.type() != Proxy.Type.HTTP) {
	    throw new IllegalArgumentException(proxy.toString());
	}
	InetSocketAddress addr = (InetSocketAddress)proxy.address();
	hostname = addr.getHostName();
	port = addr.getPort();
	id = "proxy-" + hostname;
    }

    // Implement IConnectionSpecification

    public Type getType() {
	return type;
    }

    public int getPort() {
	return port == -1 ? type.getDefaultPort() : port;
    }

    public String getIdentifier() {
	return id;
    }

    public String getHostname() {
	return hostname;
    }

    public ICredential getCredential() {
	return CREDENTIALS.getCredential(id);
    }

    public IConnectionSpecification getGateway() {
	return gateway;
    }
}
