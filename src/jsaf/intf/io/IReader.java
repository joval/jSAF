// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.io;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import jsaf.intf.util.ILoggable;

/**
 * A convenience interface for reading data.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IReader extends ILoggable {
    /**
     * Read a byte.
     *
     * @since 1.0
     */
    int read() throws IOException;

    /**
     * Get the underlying InputStream.
     *
     * @since 1.0
     */
    InputStream getStream();

    /**
     * Read a line of text, or read up until the end of the underlying stream.  Returns null if the stream is closed
     * or the end of the input has previously been reached.
     *
     * @since 1.0
     */
    String readLine() throws IOException;

    /**
     * Read a line of text, or read up until the end of the underlying stream, using the specified character set.
     * Returns null if the stream is closed or the end of the input has previously been reached.
     *
     * @since 1.0.2
     */
    String readLine(Charset charset) throws IOException;

    /**
     * Read buff.length bytes.
     *
     * @since 1.0
     */
    void readFully(byte[] buff) throws IOException;

    /**
     * Read from the stream until the specified byte is encountered, and return the bytes read.
     *
     * @throws EOFException if the end of the stream is reached
     *
     * @since 1.0
     */
    byte[] readUntil(int ch) throws IOException;

    /**
     * Close the reader.  After this method is called, all read subsequent calls will fail.
     *
     * @since 1.0
     */
    void close() throws IOException;

    /**
     * Returns true iff the close() method has ever been called.
     *
     * @since 1.0
     */
    boolean checkClosed();

    /**
     * Returns true if the end of the underlying stream has been reached.
     *
     * @since 1.0
     */
    boolean checkEOF();

    /**
     * Set a checkpoint to which the reader can be reset.
     *
     * @since 1.0
     */
    void setCheckpoint(int readAheadLimit) throws IOException;

    /**
     * Return the stream to the last mark position.
     *
     * @since 1.0
     */
    void restoreCheckpoint() throws IOException;
}
