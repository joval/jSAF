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
     * Property governing whether the filesystem cache layer should be JDBM-backed (true) or memory-backed (false).
     *
     * @since 1.2
     */
    String PROP_CACHE_JDBM = "fs.cache.useJDBM";

    /**
     * Property governing whether searches for un-anchored path expressions are permitted. For example, '^/tmp' is
     * a left-anchored regular expression, but '/tmp' is not. Un-anchored expressions require searching all mounted
     * filesystems, and typically reflect a configuration error (as opposed to a legitimate search).
     *
     * @since 1.3.9
     */
    String PROP_FS_SEARCH_ALLOW_UNANCHORED = "fs.search.allowUnanchored";

    /**
     * Property governing the maximum number of filesystem search error messages to keep.
     *
     * @since 1.3.9
     */
    String PROP_FS_SEARCH_MAX_ERRORS = "fs.search.maxErrors";

    /**
     * Property governing the maximum number of filesystem search warning messages to keep.
     *
     * @since 1.3.9
     */
    String PROP_FS_SEARCH_MAX_WARNINGS = "fs.search.maxWarnings";

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
     * Returns the maximum length of a command that should be used with createProcess.
     *
     * @since 1.2
     */
    int maxCommandLength();

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
