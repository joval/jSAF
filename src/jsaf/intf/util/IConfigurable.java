// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * Something with properties.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IConfigurable {
    /**
     * Get the properties (which is what makes the object configurable).
     *
     * @since 1.0
     */
    IProperty getProperties();
}
