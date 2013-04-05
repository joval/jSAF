// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

import java.util.Properties;

/**
 * An interface representing something that can have properties. Iterates on the property keys.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IProperty extends Iterable<String> {
    /**
     * Returns whether or not the specified key maps to a value.
     *
     * @since 1.0.1
     */
    boolean containsKey(String key);

    /**
     * Returns the value associates with the key.  Returns null if there is no corresponding value defined for the key.
     *
     * @since 1.0
     */
    String getProperty(String key);

    /**
     * Returns the value of key as an int.  Returns 0 if there is no corresponding value defined for the key.
     *
     * @since 1.0
     */
    int getIntProperty(String key);

    /**
     * Returns the value of key as an long.  Returns 0 if there is no corresponding value defined for the key.
     *
     * @since 1.0
     */
    long getLongProperty(String key);

    /**
     * Returns the value of key as a boolean.  Returns false if there is no corresponding value defined for the key.
     *
     * @since 1.0
     */
    boolean getBooleanProperty(String key);

    /**
     * Set the value for the specified key.  Set the value to null to remove the property.
     *
     * @since 1.0
     */
    void setProperty(String key, String value);

    /**
     * Convert to Java Properties.
     *
     * @since 1.0
     */
    Properties toProperties();
}
