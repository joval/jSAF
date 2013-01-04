// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io.fs;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import jsaf.intf.io.IRandomAccess;

/**
 * File access layer interface.  Every IFile implementation is backed by an accessor, which is responsible for
 * interacting with the actual file.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IAccessor {
    boolean exists();
    DefaultMetadata getInfo() throws IOException;
    long getCtime() throws IOException;
    long getMtime() throws IOException;
    long getAtime() throws IOException;
    long getLength() throws IOException;
    IRandomAccess getRandomAccess(String mode) throws IOException;
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream(boolean append) throws IOException;
    String getCanonicalPath() throws IOException;
    String[] list() throws IOException;
    boolean mkdir();
    void delete() throws IOException;
}
