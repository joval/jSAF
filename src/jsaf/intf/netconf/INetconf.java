// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.netconf;

import org.w3c.dom.Document;

import jsaf.intf.system.ISession;

/**
 * An interface for a session that can perform NETCONF operations.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface INetconf extends ISession {
    /**
     * Get an XML Document containing an unfiltered get-config reply.
     */
    Document getConfig() throws Exception;
}
