// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.powershell;

import java.io.InputStream;
import java.io.IOException;

import jsaf.intf.util.ILoggable;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.provider.windows.powershell.PowershellException;

/**
 * An interface to a Powershell 2.0 runspace.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRunspace extends ILoggable {
    /**
     * Get a unique identifier for this runspace.
     *
     * @since 1.0
     */
    String getId();

    /**
     * Load a Powershell module into the runspace from a stream.
     *
     * @throws IOException if there is a problem reading from the input, or writing to the Runspace
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.0
     */
    void loadModule(InputStream in) throws IOException, PowershellException;

    /**
     * Load a module with the specified read timeout (in millis).
     *
     * @since 1.0
     */
    void loadModule(InputStream in, long timeout) throws IOException, PowershellException;

    /**
     * Load an assembly (DLL) into the runspace from a stream.
     *
     * @throws IOException if there is a problem reading from the input, or writing to the Runspace
     * @throws PowershellException if there is an error loading the assembly
     *
     * @since 1.1
     */
    void loadAssembly(InputStream in) throws IOException, PowershellException;

    /**
     * Load an assembly with the specified read timeout (in millis).
     *
     * @since 1.1
     */
    void loadAssembly(InputStream in, long timeout) throws IOException, PowershellException;

    /**
     * Invoke a command or module.
     *
     * @return Text output from the command
     *
     * @throws IOException if there is a problem reading or writing data to/from the Runspace
     * @throws PowershellException if the command causes Powershell to raise an exception
     *
     * @since 1.0
     */
    String invoke(String command) throws IOException, PowershellException;

    /**
     * Invoke a command or module with the specified read timeout (in millis).
     *
     * @since 1.0
     */
    String invoke(String command, long timeout) throws IOException, PowershellException;

    /**
     * Get the current prompt String.
     *
     * @since 1.0
     */
    String getPrompt();

    /**
     * Get the view for this runspace.
     *
     * @since 1.0
     */
    IWindowsSession.View getView();

    /**
     * Returns whether the runspace's process is still alive.
     *
     * @since 1.1
     */
    boolean isAlive();

    /**
     * Returns whether this runspace is being used by any thread.
     *
     * @since 1.1.2
     */
    boolean isBusy();
}
