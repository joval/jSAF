// Copyright (c) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.io;

import java.io.IOException;

/**
 * A platform-independent interface providing random-access to a file.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRandomAccess {
    /**
     * Read buff.length bytes.
     *
     * @since 1.0
     */
    public void readFully(byte[] buff) throws IOException;

    /**
     * Close the IRandomAccess and its underlying resources.
     *
     * @since 1.0
     */
    public void close() throws IOException;

    /**
     * Set the position of the file pointer.
     *
     * @since 1.0
     */
    public void seek(long pos) throws IOException;

    /**
     * Read a byte.
     *
     * @since 1.0
     */
    public int read() throws IOException;

    /**
     * Read into a buffer.  Doesn't necessarily fill buff; returns the number of bytes read.
     *
     * @since 1.0
     */
    public int read(byte[] buff) throws IOException;

    /**
     * Read at most len bytes into a buffer, starting at offset.
     *
     * @since 1.0
     */
    public int read(byte[] buff, int offset, int len) throws IOException;

    /**
     * Return the length of the underlying file.
     *
     * @since 1.0
     */
    public long length() throws IOException;

    /**
     * Get the current position in the IRandomAccess.
     *
     * @since 1.0
     */
    public long getFilePointer() throws IOException;
}
