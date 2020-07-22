// Copyright (C) 2019 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.ssh.discovery;

import java.io.IOException;

import jsaf.discovery.DiscoveryException;
import jsaf.intf.remote.IConnectionSpecification;
import jsaf.intf.discovery.IDiscoveryService;

/**
 * An interface extending the generic IDiscoveryService, adding SSH-related capabilities.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.4
 */
public interface ISshDiscoveryService extends IDiscoveryService {
    /**
     * Instances return a Result, potentially also having an SSH fingerprint.
     */
    @Override
    SshResult discover(IConnectionSpecification target) throws DiscoveryException;

    /**
     * Decrypt an SSH private key.
     */
    byte[] decryptPrivateKey(byte[] encrypted, char[] passphrase) throws IOException;

    /**
     * An interface for discovery result information.
     */
    public interface SshResult extends Result {
	/**
	 * @return the fingerprint of the SSH public key (if Type is SSH).
	 */
        String getFingerprint();
    }
}
