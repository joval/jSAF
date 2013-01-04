// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * An interface for interacting with an object that can "expire".
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IPerishable {
    /**
     * Check whether this object has expired.
     */
    boolean checkExpired();

    /**
     * Set the object's shelf life.  This (re-)starts the expiration timer.
     */
    void setTimeout(long timeout);
}
