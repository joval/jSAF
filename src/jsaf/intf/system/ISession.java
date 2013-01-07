// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.system;

import java.io.IOException;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;

/**
 * A representation of a session.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ISession extends IBaseSession {
    public void setWorkingDir(String path);

    /**
     * Get the path to the "temp" directory.
     */
    public String getTempDir() throws IOException;

    /**
     * Get the machine's name. NOTE: This name might not be meaningful to DNS.
     */
    String getMachineName();

    public IFilesystem getFilesystem();

    public IEnvironment getEnvironment();
}
