// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.remote;

/**
 * An interface that encapsulates access to all the routing and credential information required to connect to target hosts.
 * This interface was introduced in version 1.0.2 to replace IRemote, in order to eliminate its inherent limitations.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0.2
 */
public interface IConnectionSpecificationFactory {
    /**
     * Return a specification for connecting to the specified target.
     *
     * @param target specifies some unique identifier for a target host that is understood by the implementation class.
     */
    IConnectionSpecification getSpecification(String target);
}
