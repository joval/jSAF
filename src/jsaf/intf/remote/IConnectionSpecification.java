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
	SOCKS_PROXY(1080),
	HTTP_PROXY(8080),
	SSH(22),
	WINDOWS(5985),
	UNKNOWN(-1);

	Type(int port) {
	    this.port = port;
	}

	private int port;

	public int getDefaultPort() {
	    return port;
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
            if (System.getProperty("os.name").startsWith("Windows")) {
                return Type.WINDOWS;
            } else {
                return Type.UNKNOWN;
            }
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
