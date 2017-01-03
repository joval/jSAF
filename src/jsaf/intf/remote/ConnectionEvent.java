// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.remote;

/**
 * An enumeration of connection event types for use with the IPublisher/ISubscriber event notification interfaces.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public enum ConnectionEvent {
    /**
     * An event indicating that endpoint type discovery is being performed. The argument will be a String id
     * corresponding to IConnectionSpecification.getIdentifier().
     */
    DISCOVERY,

    /**
     * An event indicating that endpoint type discovery has failed. The argument will be a String id corresponding
     * to IConnectionSpecification.getIdentifier().
     */
    DISCOVERY_FAILURE,

    /**
     * An event indicating that endpoint type discovery has been successfully performed. The argument will be a String
     * id corresponding to IConnectionSpecification.getIdentifier().
     */
    DISCOVERY_SUCCESS,

    /**
     * An event indicating that a connection is being initiated. The argument will be a String id corresponding
     * to IConnectionSpecification.getIdentifier().
     */
    CONNECT,

    /**
     * An event indicating that a connection could not be established. The argument will be a String id corresponding
     * to IConnectionSpecification.getIdentifier().
     */
    CONNECT_FAILURE,

    /**
     * An event indicating that a connection was established successfully. The argument will be a String id corresponding
     * to IConnectionSpecification.getIdentifier().
     */
    CONNECT_SUCCESS,

    /**
     * An event indicating that the primary authentication process has started. The argument will be a String id
     * corresponding to IConnectionSpecification.getIdentifier().
     */
    AUTH,

    /**
     * An event indicating that primary authentication has failed. The argument will be a String id corresponding to
     * IConnectionSpecification.getIdentifier().
     */
    AUTH_FAILURE,

    /**
     * An event indicating that primary authentication has succeeded. The argument will be a String id corresponding to
     * IConnectionSpecification.getIdentifier().
     */
    AUTH_SUCCESS,

    /**
     * An event indicating that a privilege escalation (secondary authentication) test has started. The argument will be
     * a String id corresponding to IConnectionSpecification.getIdentifier().
     *
     * @see jsaf.intf.ssh.identity.ISshCredential#getPrivilegeEscalation()
     */
    PRIVILEGE_ESCALATION_TEST,

    /**
     * An event indicating that a privilege escalation (secondary authentication) test has failed. The argument will be
     * a String id corresponding to IConnectionSpecification.getIdentifier().
     *
     * @see jsaf.intf.ssh.identity.ISshCredential#getPrivilegeEscalation()
     */
    PRIVILEGE_ESCALATION_FAILURE,

    /**
     * An event indicating that a privilege escalation (secondary authentication) test has succeeded. The argument will be
     * a String id corresponding to IConnectionSpecification.getIdentifier().
     *
     * @see jsaf.intf.ssh.identity.ISshCredential#getPrivilegeEscalation()
     */
    PRIVILEGE_ESCALATION_SUCCESS,

    /**
     * An event indicating that a privilege escalation test has started. The argument will be a String id corresponding
     * to IConnectionSpecification.getIdentifier().
     */
    DISCONNECT;
}
