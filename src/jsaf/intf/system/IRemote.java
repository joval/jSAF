// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.system;

import java.io.IOException;
import java.io.File;

import jsaf.intf.identity.ICredentialStore;

/**
 * Remote management interface for an ISession factory.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRemote {
    /**
     * Set the ICredentialStore for the SessionFactory.
     *
     * @since 1.0
     */
    void setCredentialStore(ICredentialStore cs);

    /**
     * Add an SSH gateway through which an SSH destination must be contacted. Gateways can be chained together, for
     * example, to achieve the connection topology A->B->C->D, simply:
     *
     * &lt;pre&gt;
     * SessionFactory factory = SessionFactory.newInstance();
     * IRemote remote = factory.getRemote();
     * remote.addRoute("D", "C");
     * remote.addRoute("C", "B"); 
     * remote.addRoute("B", "A"); 
     * ISession session = factory.createSession("D");
     * &lt;/pre&gt;
     *
     * @since 1.0
     */
    void addRoute(String destination, String gateway);

    /**
     * Enable/disable SSH host validation. The default state is enabled (true).
     *
     * @since 1.0
     */
    void setHostValidation(boolean enable);

    /**
     * Set the SSH host database file to use for host validation.
     *
     * @since 1.0
     */
    void setKnownHosts(File f) throws IOException;
}
