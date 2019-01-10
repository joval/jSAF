// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.remote;

import jsaf.intf.identity.ICredential;
import jsaf.intf.system.ISession;

/**
 * An interface that encapsulates all the routing and credential information required to connect to a target host.
 * This targeting interface was introduced in version 1.1, allowing for environments where hostnames are not unique
 * identifiers.  It replaces the IRemote interface in 1.0.1.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.1
 */
public interface IConnectionSpecification {
    /**
     * An enumeration of connection node types.
     */
    enum Type {
	/**
	 * Indicates a SOCKS proxy.
	 */
	SOCKS_PROXY("proxy/socks", 1080),

	/**
	 * Indicates an HTTP proxy.
	 */
	HTTP_PROXY("proxy/http", 8080),

	/**
	 * Indicates an SSH-enabled device.
	 */
	SSH("ssh", 22),

	/**
	 * Indicates a WS-Management-enabled device.
	 */
	WS_MAN("WS-Management", 5985),

	/**
	 * Indicates a WS-Management-over-TLS-enabled device.
	 *
	 * @since 1.3.5
	 */
	WS_MAN_TLS("WS-Management/TLS", 5986),

	/**
	 * Indicates a device type that is unknown.
	 */
	UNKNOWN("unknown", -1);

	private String value;
	private int defaultPort;

	private Type(String value, int defaultPort) {
	    this.value = value;
	    this.defaultPort = defaultPort;
	}

	/**
	 * Get the Type's corresponding String value.
	 *
	 * @since 1.3.5
	 */
	public String value() {
	    return value;
	}

	/**
	 * Get the default port number for the Type.
	 *
	 * @since 1.1
	 */
	public int getDefaultPort() {
	    return defaultPort;
	}

	/**
	 * Given a String value, obtain a corresponding Type.
	 *
	 * @since 1.3.5
	 */
	public static Type typeOf(String s) {
	    for (Type t : values()) {
		if (t.value.equals(s)) {
		    return t;
		}
	    }
	    return UNKNOWN;
	}
    }

    /**
     * Get the type of the target.
     */
    Type getType();

    /**
     * Get a unique identifier for the node, which is persistent across invocations of the application.
     */
    String getIdentifier();

    /**
     * Return the DNS hostname of the target.
     */
    String getHostname();

    /**
     * Get the port through which the node can be reached, or -1 if unknown.
     */
    int getPort();

    /**
     * For SSH nodes, returns the expected host public key fingerprint.
     *
     * If any fingerprint is permissible, returns "*".
     *
     * @throws IllegalStateException if the node is not Type.SSH.
     */
    String getFingerprint();

    /**
     * Return the credential information that should be used to connect to the target.
     */
    ICredential getCredential();

    /**
     * Return the connection specification about a proxy or SSH gateway through which the host can be reached.
     *
     * @return null if the host can be reached directly
     */
    IConnectionSpecification getGateway();

    /**
     * A convenience IConnectionSpecification for the local machine.
     */
    IConnectionSpecification LOCALHOST = new IConnectionSpecification() {
	public Type getType() {
	    return Type.UNKNOWN;
	}

	public String getIdentifier() {
	    return ISession.LOCALHOST;
	}

	public String getHostname() {
	    return ISession.LOCALHOST;
	}

	public int getPort() {
	    return 0;
	}

	public String getFingerprint() {
	    return null;
	}

	public ICredential getCredential() {
	    return null;
	}

	public IConnectionSpecification getGateway() {
	    return null;
	}
    };
}
