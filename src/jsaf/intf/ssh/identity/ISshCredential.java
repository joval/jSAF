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
     * @deprecated Use getPrivilegeEscalation.
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

    /**
     * Get the PrivilegeEscalation interface associated with this SSH credential.
     *
     * @since 1.0.2
     */
    PrivilegeEscalation getPrivilegeEscalation();

    /**
     * An interface defining different privilege escalation types for SSH sessions.
     *
     * @since 1.0.2
     */
    public interface PrivilegeEscalation {
	public enum Type {
	    /**
	     * Type indicating that there is no escalation available for this credential.
	     */
	    NONE,

	    /**
	     * Type indicating that escalation of privilege is performed using a substitute identity, such as the
	     * root account.
	     */
	    SU,

	    /**
	     * Type indicating that escalation of privilege is performed using sudo, i.e., the current account in a
	     * privileged mode.
	     */
	    SUDO,

	    /**
	     * Type indicating that escalation of privilege is performed using the IOS "enable" command.
	     */
	    IOS_ENABLE;
	}

	/**
	 * Get the escalation type.
	 */
	Type getType();

	/**
	 * Retrieve the credential associated with the escalation type.
	 *
	 * @return null for Type.NONE and Type.SUDO, ICredential for Type.ROOT and Type.IOS_ENABLE.
	 */
	ICredential getCredential();
    }
}
