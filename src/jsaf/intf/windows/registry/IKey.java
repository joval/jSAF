// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import jsaf.provider.windows.registry.RegistryException;

/**
 * Interface for a Windows registry key.  Can be used in conjunction with an IRegistry to browse child keys and values.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IKey {
    /**
     * Get a string representation of this key.  Corresponds to getHive() + \\ + getPath().
     *
     * @since 1.0
     */
    String toString();

    /**
     * Get the hive for this key.
     *
     * @since 1.0
     */
    IRegistry.Hive getHive();

    /**
     * Get the full path for this key underneath its hive.
     *
     * @since 1.0
     */
    String getPath();

    /**
     * Get this key's name (the last element of its path).
     *
     * @since 1.0
     */
    String getName();

    /**
     * Indicates whether this key has a subkey with the given name.
     *
     * @since 1.0
     */
    boolean hasSubkey(String name);

    /**
     * Returns an array of subkey names.
     *
     * @since 1.0
     */
    String[] listSubkeys() throws RegistryException;

    /**
     * Returns an array of subkey names matching (filtered by) the given Pattern.
     *
     * @since 1.0
     */
    String[] listSubkeys(Pattern p) throws RegistryException;

    /**
     * Return a child of this key.
     *
     * @since 1.0
     */
    IKey getSubkey(String name) throws NoSuchElementException, RegistryException;

    /**
     * Test if this key has a value with the given name.
     *
     * @since 1.0
     */
    boolean hasValue(String name);

    /**
     * Returns an array of the names of this key's values.
     *
     * @since 1.0
     */
    IValue[] listValues() throws RegistryException;

    /**
     * Returns an array of the names of this key's values matching (filtered by) the given Pattern.
     *
     * @since 1.0
     */
    IValue[] listValues(Pattern p) throws RegistryException;

    /**
     * Get a value of this key.
     *
     * @since 1.0
     */
    IValue getValue(String name) throws NoSuchElementException, RegistryException;
}
