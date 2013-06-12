// Copyright (C) 2012 jOVAL.org.  All rights reserved.
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
     * The series of dashes appearing before and after descriptive section header text.
     *
     * @since 1.0
     */
    String DASHES = "------------------";

    /**
     * IOS command string "show running-config"
     *
     * @since 1.0
     */
    String GLOBAL = "show running-config";

    /**
     * IOS command string "show version"
     *
     * @since 1.0.2
     */
    String VERSION = "show version";

    /**
     * IOS command string "show interfaces"
     *
     * @since 1.0.2
     */
    String INTERFACES = "show interfaces";

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
