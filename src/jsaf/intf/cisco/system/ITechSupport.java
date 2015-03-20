// Copyright (C) 2015 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.cisco.system;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An interface for accessing data from the "show tech-support" IOS command.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ITechSupport {
    /**
     * Cisco IOS, IOS-XE and ASA all have similar tech-support information, but don't necessarily use identical commands.  For example,
     * Cisco IOS and IOS-XE use the command "show interfaces" to list IPv4 interface information, but Cisco ASA uses the command
     * "show interface" (singular).  This abstraction makes it possible for an ITechSupport to be shared by the different systems, which
     * can provide their own constants.
     *
     * @since 1.3.1
     */
    interface Constants {
	/**
	 * The string of dashes used to demarcate commands in show-tech output.
	 */
	String dashes();

	/**
	 * The command for showing the running configuration.
	 */
	String global();

	/**
	 * The command for showing version information.
	 */
	String version();

	/**
	 * The command for listing information about IPv4 interfaces.
	 */
	String ip4interfaces();

	/**
	 * The command for listing information about IPv6 interfaces.
	 */
	String ip6interfaces();
    }

    /**
     * A list of subcommands for which information is available.
     *
     * @since 1.0
     */
    Collection<String> getShowSubcommands();

    /**
     * A complete list of all the "headings" for which information is available.  This includes all the show subcommands.
     *
     * @since 1.0
     */
    Collection<String> getHeadings();

    /**
     * Fetches the response lines associated with the given heading.
     *
     * @throws NoSuchElementException if the heading is not found.
     *
     * @since 1.0
     */
    List<String> getLines(String heading) throws NoSuchElementException;

    /**
     * Fetches the raw data associated with the given heading.
     *
     * @throws NoSuchElementException if the heading is not found.
     *
     * @since 1.0
     */
    String getData(String heading) throws NoSuchElementException;
}
