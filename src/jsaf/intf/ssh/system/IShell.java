// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.ssh.system;

import java.io.IOException;

import jsaf.intf.system.IProcess;
import jsaf.provider.SessionException;

/**
 * An interface to a shell channel.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IShell extends IProcess {
    /**
     * Waiting at a prompt?
     *
     * @since 1.0
     */
    boolean ready();

    /**
     * Send a line of text.
     *
     * @throws IllegalStateException if not ready
     * @throws SessionException if there was a problem with the session
     *
     * @since 1.0
     */
    void println(String str) throws IllegalStateException, SessionException, IOException;

    /**
     * Read from the shell until there is either (1) a new prompt, or (2) the timeout has been reached.
     *
     * @throws IllegalStateException if the shell is being used to run an IProcess
     * @throws InterruptedIOException if the timeout expires
     *
     * @since 1.0
     */
    String read(long timeout) throws IllegalStateException, IOException;

    /**
     * Read a single line from the shell until there is either (1) a new prompt, or (2) the timeout has been reached.
     *
     * @throws IllegalStateException if the shell is being used to run an IProcess
     * @throws InterruptedIOException if the timeout expires
     *
     * @since 1.0
     */
    String readLine(long timeout) throws IllegalStateException, IOException;

    /**
     * Get the current prompt String.
     *
     * @since 1.0
     */
    String getPrompt();

    /**
     * Close the shell. If keepAlive=true, this call is ignored.
     *
     * @since 1.0
     */
    void close() throws IllegalStateException;
}
