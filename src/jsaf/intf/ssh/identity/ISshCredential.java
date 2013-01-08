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
 */
public interface ISshCredential extends ICredential {
    ICredential getRootCredential();
    String getPassphrase();
    File getPrivateKey();
}
