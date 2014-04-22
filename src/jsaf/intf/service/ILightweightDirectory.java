// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.service;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.InitialLdapContext;

import jsaf.intf.identity.ICredential;

/**
 * A session service interface for interacting with an LDAP server.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ILightweightDirectory {
    /**
     * An enumeration of protocol security options.
     */
    enum Security {
        NONE(389),
        SSL(636),
        TLS(389);

        private int port;

        private Security(int port) {
            this.port = port;
        }

        public int getDefaultPort() {
            return port;
        }
    }

    /**
     * Get an LdapContext to the specified LDAP server.
     *
     * @param uri    The base URI for the context
     * @param cred   The credential to use to bind to the LDAP server.
     *
     * @since 1.0
     */
    LdapContext getContext(String uri, ICredential cred) throws NamingException;
}
