// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface representation of a process.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IProcess {
    /**
     * Return the command used to spawn the process.
     *
     * @since 1.0
     */
    public String getCommand();

    /**
     * Start the process.
     *
     * @since 1.0
     */
    public void start() throws Exception;

    /**
     * Get the process's stdout
     *
     * @since 1.0
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Get the process's stderr. Note: this could potentially return null, for example, if the process is run via SSH using
     * a pseudo-terminal.
     *
     * @since 1.0
     */
    public InputStream getErrorStream() throws IOException;

    /**
     * Get the process's stdin
     *
     * @since 1.0
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Wait for the process to complete.
     *
     * @param millis The maximum number of milliseconds to wait.  Set to 0 to wait until the process finishes (potentially
     *               forever).
     *
     * @since 1.0
     */
    public void waitFor(long millis) throws InterruptedException;

    /**
     * Get the exit code returned by the process.
     *
     * @throws IllegalThreadStateException if the process is still running, or was not started.
     *
     * @since 1.0
     */
    public int exitValue() throws IllegalThreadStateException;

    /**
     * Destroy the process. Generally this is called if the process has stopped responding, or if it's taking too long to
     * return.
     *
     * @since 1.0
     */
    public void destroy();

    /**
     * Query whether the process is still running.
     *
     * @since 1.0
     */
    public boolean isRunning();
}
