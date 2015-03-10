// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.cisco.system;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import jsaf.intf.netconf.INetconf;
import jsaf.intf.system.IComputerSystem;
import jsaf.intf.ssh.system.IShell;

/**
 * A representation of an IOS command-line session.
 *
 * Implementations should also implement IComputerSystem (specifically, the createProcess method).
 *
 * @see jsaf.intf.system.IComputerSystem
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IIosSession extends IComputerSystem {
    /**
     * The IOS command to enable privilege level 15 (the highest privilege level).
     */
    String ENABLE_COMMAND = "enable 15";

    /**
     * Basic IOS command modes.
     * See http://www.cisco.com/en/US/docs/ios/12_2/configfun/configuration/guide/fcf019.html#wp1000901
     *
     * @since 1.1
     */
    enum CommandMode {
	/**
	 * TCL shell mode.
	 */
	TCLSH("\\(tcl\\)#$"),

	/**
	 * Non-privileged exec mode.
	 */
	USER_EXEC(">$"),

	/**
	 * Privileged exec mode.
	 */
	PRIVILEGED_EXEC("(?!\\(tcl\\))#$"),

	/**
	 * Configuration mode.
	 */
	CONFIGURE("\\(config.*\\)#$");

	private Pattern pattern;

	private CommandMode(String pattern) {
	    try {
		this.pattern = Pattern.compile(pattern);
	    } catch (PatternSyntaxException e) {
		throw new RuntimeException(e);
	    }
	}

	/**
	 * Determine the command mode from the shell prompt.
	 */
	public static CommandMode fromPrompt(String prompt) throws IllegalArgumentException {
	    for (CommandMode mode : values()) {
		if (mode.pattern.matcher(prompt).find()) {
		    return mode;
		}
	    }
	    throw new IllegalArgumentException(prompt);
	}
    }

    /**
     * Determine whether this session has access to level 15 privileges.
     *
     * @since 1.3
     */
    boolean privileged();

    /**
     * Retrieve "show tech-support" data from the device.
     *
     * @since 1.0
     */
    ITechSupport getTechSupport();

    /**
     * Obtain a shell connection to the device.
     *
     * @since 1.0
     */
    IShell getShell() throws Exception;

    /**
     * Cast this IOS session to an INetconf.  Since Java does not permit polymorphism by inheritance, this method
     * serves that purpose.
     *
     * @since 1.1
     */
    INetconf asNetconf();
}
