// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.discovery;

import jsaf.intf.ssh.ISshTools;
import jsaf.intf.remote.IConnectionSpecification;
import jsaf.intf.system.ISession;
import jsaf.intf.util.ILoggable;

/**
 * An interface describing discovery-related utilities, useful for discovering connection-related information about
 * remote devices.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3
 */
public interface IDiscoveryService extends ILoggable {
    /**
     * @see ISshTools
     */
    ISshTools getSshTools();

    /**
     * Discover the type of an IConnectionSpecification whose type is Type.UNKNOWN.
     */
    Result discover(IConnectionSpecification target) throws Exception;

    /**
     * An interface for discovery result information.
     */
    public interface Result {
	/**
	 * Return the session type discovered for the device.
	 */
        ISession.Type getType();

	/**
	 * Return the fingerprint of the SSH public key (if Type is SSH).
	 */
        String getFingerprint();
    }
}
