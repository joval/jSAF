// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.net.InetSocketAddress;
import java.net.Proxy;

import jsaf.intf.identity.ICredential;
import jsaf.intf.identity.ICredentialStore;
import jsaf.intf.remote.IConnectionSpecification;

/**
 * An abstract implementation of IConnectionSpecification. Subclasses must populate the ICredentialStore.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.1
 */
public abstract class ConnectionSpecification implements IConnectionSpecification {
    protected int port = -1;
    protected Type type;
    protected String id, hostname, fingerprint;
    protected IConnectionSpecification gateway;
    protected ICredentialStore cs;

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
	switch(proxy.type()) {
	  case HTTP:
	    type = Type.HTTP_PROXY;
	    break;
	  case SOCKS:
	    type = Type.SOCKS_PROXY;
	    break;
	  default:
	    throw new IllegalArgumentException(proxy.toString());
	}
	InetSocketAddress addr = (InetSocketAddress)proxy.address();
	hostname = addr.getHostName();
	port = addr.getPort();
	id = "proxy" + port + "-" + hostname;
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

    public String getFingerprint() {
	switch(type) {
	  case SSH:
	    return fingerprint;
	  case UNKNOWN:
	    type = Type.SSH;
	    return fingerprint;
	  default:
	    throw new IllegalStateException(type.toString());
	}
    }

    public String getHostname() {
	return hostname;
    }

    public ICredential getCredential() {
	return cs.getCredential(hostname);
    }

    public IConnectionSpecification getGateway() {
	return gateway;
    }
}
