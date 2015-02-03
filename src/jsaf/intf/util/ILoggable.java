// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

import org.slf4j.cal10n.LocLogger;

/**
 * An interface representing something that can have a logger.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ILoggable {
    /**
     * Get the active logger for this object.
     *
     * @since 1.0
     */
    LocLogger getLogger();

    /**
     * Set the active logger for this object.
     *
     * @since 1.0
     */
    void setLogger(LocLogger logger);

    /**
     * An interface describing a utility that can be used in conjunction with ILoggable objects, to censor
     * specific character sequences (e.g., passwords) from log output.
     *
     * @since 1.3
     */
    interface Censor {
	/**
	 * Add a character sequence which the censor should redact from log messages.
	 */
	void addKeyword(char[] keyword);

	/**
	 * Clear the list of keywords that the censor redacts from log messages.
	 */
	void clearKeywords();

	/**
	 * Check if the censor is censoring a particular ILoggable.
	 */
	boolean censoring(ILoggable subject);

	/**
	 * Add an ILoggable to the censor. This will use subject.getLogger and subject.setLogger to wrap the subject's
	 * logger with a special filter that removes keywords from log messages.
	 *
	 * @throws IllegalStateException if the subject is already being censored
	 */
	void add(ILoggable subject) throws IllegalStateException;

	/**
	 * Stop censoring messages for the specified ILoggable. This will use subject.setLogger to assign the original,
	 * unfiltered logger instance.
	 *
	 * @throws IllegalStateException if the subject is not being censored
	 */
	void remove(ILoggable subject) throws IllegalStateException;
    }
}
