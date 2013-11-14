// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.system;

import java.io.File;
import java.io.IOException;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;

/**
 * A representation of a session on a computer system, which is an ISession on which you can potentially run processes
 * and access files.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.1
 */
public interface IComputerSystem extends ISession {
    /**
     * Property indicating the number of times to re-try running a command in the event of an unexpected disconnect.
     *
     * @since 1.1
     */
    String PROP_EXEC_RETRIES = "exec.retries";

    /**
     * Get the machine's own impression of its name. This name might not be meaningful to DNS.
     *
     * @since 1.1
     */
    String getMachineName();

    /**
     * Get the number of milliseconds since midnight 1/1/70 UTC, on the system.
     *
     * @since 1.1
     */
    long getTime() throws Exception;

    /**
     * Create a process on the machine, with the specified environment variables.
     *
     * @param command the command string
     * @param env Environment variables, each of the form "VARIABLE=VALUE" (can be null)
     * @param dir the directory from which to launch the process (can be null)
     *
     * @since 1.1
     */
    IProcess createProcess(String command, String[] env, String dir) throws Exception;

    /**
     * Get the path to the system's "temp" directory.
     *
     * @since 1.1
     */
    public String getTempDir() throws IOException;

    /**
     * Get access to the machine filesystem.
     *
     * @since 1.1
     */
    public IFilesystem getFilesystem();

    /**
     * Returns a copy of the user environment.
     *
     * @since 1.1
     */
    public IEnvironment getEnvironment();
}
