// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.system;

import java.io.File;
import java.io.IOException;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.IProperty;

/**
 * A representation of a session.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ISession extends ILoggable {
    /**
     * An enumeration of possible session types.
     */
    enum Type {
	/**
	 * An SSH-type session. This type is only intended to be a transition state used during a discovery process, that
	 * will ultimately yield a type of UNIX, CISCO_IOS, JUNIPER_JUNOS or UNKNOWN.
	 */
	SSH("ssh"),

	/**
	 * Indicates a session with a Unix host.
	 *
	 * @see jsaf.intf.unix.system.IUnixSession
	 */
	UNIX("unix"),

	/**
	 * Indicates a session with a device running Cisco IOS.
	 *
	 * @see jsaf.intf.cisco.system.IIosSession
	 */
	CISCO_IOS("ios"),

	/**
	 * Indicates a session with a device running Juniper JunOS.
	 *
	 * @see jsaf.intf.juniper.system.IJunosSession
	 */
	JUNIPER_JUNOS("junos"),

	/**
	 * Indicates a session with a device running Apple iOS.
	 *
	 * @see jsaf.intf.apple.system.IiOSSession
	 */
	APPLE_IOS("apple_iOS"),

	/**
	 * Indicates a session with a Windows host.
	 *
	 * @see jsaf.intf.windows.system.IWindowsSession
	 */
	WINDOWS("windows"),

	/**
	 * Indicates that the session type cannot be determined.
	 */
	UNKNOWN("unknown");

	private String s;

	Type(String s) {
	    this.s = s;
	}

	public String value() {
	    return s;
	}

	public static Type typeOf(String s) {
	    for (Type t : values()) {
		if (t.s.equals(s)) {
		    return t;
		}
	    }
	    return UNKNOWN;
	}
    }

    /**
     * An enumeration of timeouts.
     */
    public enum Timeout {
	S, M, L, XL;
    }

    /**
     * Property key used to define a "small" amount of time.
     */
    String PROP_READ_TIMEOUT_S = "read.timeout.small";

    /**
     * Property key used to define a "medium" amount of time.
     */
    String PROP_READ_TIMEOUT_M = "read.timeout.medium";

    /**
     * Property key used to define a "large" amount of time.
     */
    String PROP_READ_TIMEOUT_L = "read.timeout.large";

    /**
     * Property key used to define an "extra-large" amount of time.
     */
    String PROP_READ_TIMEOUT_XL = "read.timeout.xl";

    /**
     * Property indicating whether the session should run in debug mode (true/false).
     */
    String PROP_DEBUG = "debug";

    /**
     * Property indicating the number of times to re-try running a command in the event of an unexpected disconnect.
     */
    String PROP_EXEC_RETRIES = "exec.retries";

    /**
     * A constant defining the String "localhost".
     */
    String LOCALHOST = "localhost";

    /**
     * Get the session type.
     */
    Type getType();

    /**
     * Get the timeout value corresponding to the Timeout enumeration.
     */
    long getTimeout(Timeout to);

    /**
     * Get the session's properties. See the PROP_* property keys.
     */
    IProperty getProperties();

    /**
     * Check if the session is using debugging mode.
     * Shortcut for "true".equals(ISession.getProperties().getProperty(ISession.PROP_DEBUG))
     */
    boolean isDebug();

    /**
     * Connect the session.
     */
    boolean connect();

    /**
     * Disconnect the session.
     */
    void disconnect();

    /**
     * Check whether or not the session is connected.
     */
    boolean isConnected();

    /**
     * Get the name of the host to which the session is connected.  This must be a DNS name (safe for lookup).
     */
    String getHostname();

    /**
     * Get the machine's own impression of its name. This name might not be meaningful to DNS.
     */
    String getMachineName();

    /**
     * Get the number of milliseconds since midnight 1/1/70 UTC, on the system.
     */
    long getTime() throws Exception;

    /**
     * Create a process on the machine, with the specified environment variables.
     *
     * @param command the command string
     * @param env Environment variables, each of the form "VARIABLE=VALUE" (can be null)
     * @param dir the directory from which to launch the process (can be null)
     */
    IProcess createProcess(String command, String[] env, String dir) throws Exception;

    /**
     * Get the directory in which state information for the session is stored.
     *
     * @return null if this is a stateless session.
     */
    File getWorkspace();

    /**
     * Get the path to the system's "temp" directory.
     */
    public String getTempDir() throws IOException;

    /**
     * Get access to the machine filesystem.
     */
    public IFilesystem getFilesystem();

    /**
     * Returns the account name used by this session, if any.
     */
    String getUsername();

    /**
     * Returns a copy of the user environment.
     */
    public IEnvironment getEnvironment();

    /**
     * When you're completely finished using the session, call this method to clean up caches and other resources.
     */
    void dispose();
}
