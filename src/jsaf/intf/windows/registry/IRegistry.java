// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.util.NoSuchElementException;

import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.provider.windows.registry.RegistryException;

/**
 * An interface for accessing a Windows registry.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRegistry extends ILoggable {
    /**
     * Search condition field for the hive.
     *
     * @since 1.0
     */
    int FIELD_HIVE = 100;

    /**
     * Search condition field for the key path or pattern.
     *
     * @since 1.0
     */
    int FIELD_KEY = 101;

    /**
     * Search condition field for the value name or pattern.
     *
     * @since 1.0
     */
    int FIELD_VALUE = 102;

    /**
     * Search condition field for a base-64 encoded value name.
     *
     * @since 1.0
     */
    int FIELD_VALUE_BASE64 = 103;

    /**
     * The path of the HKLM hive child key containing the computer name value.
     *
     * @since 1.0
     */
    String COMPUTERNAME_KEY	= "System\\CurrentControlSet\\Control\\ComputerName\\ComputerName";

    /**
     * The name of the value of the COMPUTERNAME_KEY key containing the computer name.
     *
     * @since 1.0
     */
    String COMPUTERNAME_VAL	= "ComputerName";

    /**
     * An enumeration of the registry hives.
     *
     * @since 1.0
     */
    enum Hive {
	/**
	 * Classes root hive.
	 *
	 * @since 1.0
	 */
	HKCR ("HKEY_CLASSES_ROOT", 0x80000000L),

	/**
	 * Current user hive.
	 *
	 * @since 1.0
	 */
	HKCU ("HKEY_CURRENT_USER", 0x80000001L),

	/**
	 * Local machine hive.
	 *
	 * @since 1.0
	 */
	HKLM ("HKEY_LOCAL_MACHINE", 0x80000002L),

	/**
	 * Users hive.
	 *
	 * @since 1.0
	 */
	HKU  ("HKEY_USERS", 0x80000003L),

	/**
	 * Current config hive (legacy).
	 *
	 * @since 1.0
	 */
	HKCC ("HKEY_CURRENT_CONFIG", 0x80000005L),

	/**
	 * Dynamic data hive (legacy).
	 *
	 * @since 1.0
	 */
	HKDD ("HKEY_DYN_DATA", 0x80000006L);

	private String name;
	private long id;

	private Hive(String name, long id) {
	    this.name = name;
	    this.id = id;
	}

	/**
	 * Get a string representation of the hive's name.
 	 *
	 * @since 1.0
	 */
	public String getName() {
	    return name;
	}

	/**
	 * Get the ID constant corresponding to the hive.
 	 *
	 * @since 1.0
	 */
	public long getId() {
	    return id;
	}

	/**
	 * Get the Hive corresponding to the specified String name, or HKLM if no match is found.
 	 *
	 * @since 1.0
	 */
	public static Hive fromName(String name) {
	    for (Hive hive : values()) {
		if (hive.getName().equals(name.toUpperCase())) {
		    return hive;
		}
	    }
	    return Hive.HKLM;
	}
    }

    /**
     * String delimiter for registry key paths.
     *
     * @since 1.0
     */
    String DELIM_STR = "\\";

    /**
     * Character delimiter for registry key paths.
     *
     * @since 1.0
     */
    char DELIM_CH = '\\';

    /**
     * Escaped String delimiter for registry key paths.
     *
     * @since 1.0
     */
    String ESCAPED_DELIM = "\\\\";

    /**
     * Get Windows license data from the registry.
     *
     * @throws Exception if there was a problem retrieving the license information.
     *
     * @since 1.0
     */
    ILicenseData getLicenseData() throws Exception;

    /**
     * Get an ISearchable for the registry.
     *
     * @since 1.0
     */
    ISearchable<IKey> getSearcher();

    /**
     * Get a particular hive.
     *
     * @since 1.0
     */
    IKey getHive(Hive hive);

    /**
     * Return a key using its full path (including hive name).
     *
     * @since 1.0
     */
    IKey getKey(String fullPath) throws NoSuchElementException, RegistryException;

    /**
     * Return a key from a hive using the specified redirection mode.
     *
     * @since 1.0
     */
    IKey getKey(Hive hive, String path) throws NoSuchElementException, RegistryException;

    /**
     * Return the child subkeys of the specified key.
     *
     * @since 1.0
     */
    IKey[] enumSubkeys(IKey key) throws RegistryException;

    /**
     * Return a particular value of a key, given its name.
     *
     * @param name use null to retrieve the default value
     *
     * @since 1.0
     */
    IValue getValue(IKey key, String name) throws NoSuchElementException, RegistryException;

    /**
     * Return all the values of a key.
     *
     * @since 1.0
     */
    IValue[] enumValues(IKey key) throws RegistryException;
}
