// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * An interface for something that requires clean-up.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3
 */
public interface IDisposable extends ILoggable {
    /**
     * Clean-up/release any resources held by the object.
     *
     * @since 1.3
     */
    void dispose();
}
