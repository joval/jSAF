// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.service;

import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import jsaf.intf.identity.ICredential;

/**
 * A session service interface for interacting with an LDAP server.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3
 */
public interface ILightweightDirectory {
    /**
     * Property key for an ISession property, to indicate the maximum desired search time.
     */
    public static final String PROP_MAX_WAIT = "ldap.search.maxMillis";

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
     * Get a new InitialLdapContext for the LDAP server associated with this ILightweightDirectory interface.
     *
     * @param cred   The credential to use to bind to the LDAP server.
     * @param baseDN The base DN for the context.
     */
    InitialLdapContext getContext(ICredential cred, String baseDN) throws NamingException;
}
