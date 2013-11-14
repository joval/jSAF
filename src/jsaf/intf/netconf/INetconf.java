// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.netconf;

import org.w3c.dom.Document;

import jsaf.intf.system.ISession;

/**
 * An interface for a session that can perform NETCONF operations.
 *
 * @see <a href="http://tools.ietf.org/html/rfc4742">RFC 4742</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface INetconf extends ISession {
    /**
     * The IANA-assigned port number for NETCONF over SSH.
     */
    int SSH_PORT = 830;

    /**
     * Get an XML Document containing an unfiltered get-config reply.
     *
     * @since 1.0
     */
    Document getConfig() throws Exception;
}
