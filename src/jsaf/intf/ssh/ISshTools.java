// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.ssh;

import jsaf.intf.util.IConnectionSpecification;

/**
 * An interface for a provider of certain SSH utility functions. This interface makes it possible for a jSAF provider
 * to offer indirect access to its SSH implementation, without making the implementation directly accessible to the
 * System classloader.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0.2
 */
public interface ISshTools {
    /**
     * Decrypts the provided encrypted bytes using the specified passphrase.  Both the encrypted bytes and passphrase
     * bytes will be zeroed-out as part of this call.  The returned byte buffer should be zeroed out as soon as possible
     * after being securely stored.
     */
    byte[] decryptPrivateKey(byte[] encrypted, byte[] passphrase) throws Exception;

    /**
     * Discover the public key of the specified target machine.
     */
    String getPublicKey(IConnectionSpecification target) throws Exception;
}
