// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * An interface for interacting with an object that can "expire".
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IPerishable {
    /**
     * Check whether this object has expired.
     *
     * @since 1.0
     */
    boolean checkExpired();

    /**
     * Set the object's shelf life.  This (re-)starts the expiration timer.
     *
     * @since 1.0
     */
    void setTimeout(long timeout);

    /**
     * Defuse the object's expiration timer.
     *
     * @since 1.0.1
     */
    void defuse();
}
