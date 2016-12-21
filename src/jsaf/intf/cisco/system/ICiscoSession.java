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
 * The base ISession interface for all Cisco devices.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.1
 */
public interface ICiscoSession extends IComputerSystem {
    /**
     * The IOS command to enable privilege level 15 (the highest privilege level).
     */
    String ENABLE_COMMAND = "enable 15";

    /**
     * Basic IOS command modes.
     * See http://www.cisco.com/en/US/docs/ios/12_2/configfun/configuration/guide/fcf019.html#wp1000901
     */
    enum CommandMode {
	/**
	 * TCL shell mode.
	 */
	TCLSH("\\(tcl\\)#\\s*$"),

	/**
	 * Non-privileged exec mode.
	 */
	USER_EXEC(">\\s*$"),

	/**
	 * Privileged exec mode.
	 */
	PRIVILEGED_EXEC("(?!\\(tcl\\))#\\s*$"),

	/**
	 * Configuration mode.
	 */
	CONFIGURE("\\(config.*\\)#\\s*$"),

	/**
	 * Indicates an authentication failure.
	 */
	AUTH_PROMPT("^Password:");

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
     */
    boolean privileged();

    /**
     * Retrieve "show tech-support" data from the device.
     */
    ITechSupport getTechSupport() throws Exception;

    /**
     * Get the tech-support constants for the implementation.
     */
    ITechSupport.Constants getTSConstants();

    /**
     * Obtain a shell connection to the device.
     */
    IShell getShell() throws Exception;
}
