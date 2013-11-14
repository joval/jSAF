// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.system;

import jsaf.Message;
import jsaf.intf.system.IComputerSystem;
import jsaf.util.SafeCLI;

/**
 * A representation of a Unix command-line session.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUnixSession extends IComputerSystem {
    /**
     * Property indicating the number of milliseconds to wait for a read before quiting a privilege escalation attempt.
     *
     * @since 1.0.1
     */
    String PROP_SUDO_READ_TIMEOUT = "elevate.read.timeout";

    /**
     * Property indicating the maximum length for any command output.
     *
     * @since 1.0.1
     */
    String PROP_SUDO_READ_MAXLEN = "elevate.read.maxlen";

    /**
     * Root username.
     *
     * @since 1.0.1
     */
    String ROOT = "root";

    /**
     * Get the "Flavor" of the Unix session.
     *
     * @since 1.0
     */
    Flavor getFlavor();

    /**
     * Enumeration of Unix flavors.
     *
     * @since 1.0
     */
    enum Flavor {
	/**
	 * Unknown (unsupported) Unix flavor.
	 *
	 * @since 1.0
	 */
	UNKNOWN("unknown"),

	/**
	 * Flavor for AIX.
	 *
	 * @since 1.0
	 */
	AIX("AIX"),

	/**
	 * Flavor for Linux.
	 *
	 * @since 1.0
	 */
	LINUX("Linux"),

	/**
	 * Flavor for Mac OS X.
	 *
	 * @since 1.0
	 */
	MACOSX("Darwin"),

	/**
	 * Flavor for Sun (Oracle) Solaris.
	 *
	 * @since 1.0
	 */
	SOLARIS("SunOS");
    
	private String value = null;
    
	private Flavor(String value) {
	    this.value = value;
	}

	/**
	 * Get the String value for the Flavor.
	 *
	 * @since 1.0
	 */
	public String value() {
	    return value;
	}

	/**
	 * Get the Flavor corresponding to the String value.
	 *
	 * @since 1.0
	 */
	public static Flavor flavorOf(String value) {
	    for (Flavor flavor : values()) {
		if (flavor.value().equals(value)) {
		    return flavor;
		}
	    }
	    return UNKNOWN;
	}

	/**
	 * Computes the Flavor of the specified session.
	 *
	 * @since 1.0
	 */
	public static Flavor flavorOf(IUnixSession session) {
	    try {
		String osName = SafeCLI.exec("uname -s", session, Timeout.S);
		for (Flavor flavor : values()) {
		    if (flavor.value().equals(osName)) {
			return flavor;
		    }
		}
		session.getLogger().warn(Message.WARNING_UNIX_FLAVOR, osName);
	    } catch (Exception e) {
		session.getLogger().warn(Message.ERROR_UNIX_FLAVOR);
		session.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return Flavor.UNKNOWN;
	}
    }
}
