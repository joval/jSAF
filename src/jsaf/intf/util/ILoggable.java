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
}
