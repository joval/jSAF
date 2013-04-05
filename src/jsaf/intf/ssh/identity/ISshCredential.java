// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.ssh.identity;

import java.io.File;

import jsaf.intf.identity.ICredential;

/**
 * A representation of an SSH credential.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISshCredential extends ICredential {
    /**
     * Get the credential for a user with "elevated" privileges (i.e., the root account).
     *
     * @since 1.0
     */
    ICredential getRootCredential();

    /**
     * Get the passphrase required to decrypt the private key file.
     *
     * @since 1.0.1
     */
    char[] getPassphrase();

    /**
     * Get the SSH private key bytes (potentially password-encrypted).
     *
     * @since 1.0.1
     */
    byte[] getPrivateKey();
}
