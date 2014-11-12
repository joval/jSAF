// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.util.NoSuchElementException;

import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.intf.util.ISearchable.Condition;
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
     * Return multiple subkeys from a hive all at once. Results will be in the same order as the paths argument. Non-existent
     * paths will be reflected by null entries in the results.
     *
     * @since 1.3
     */
    IKey[] getKeys(String[] fullPaths) throws RegistryException;

    /**
     * Return a key from a hive.
     *
     * @since 1.0
     */
    IKey getKey(Hive hive, String path) throws NoSuchElementException, RegistryException;

    /**
     * Return multiple subkeys from a hive all at once. Results will be in the same order as the paths argument. Non-existent
     * paths will be reflected by null entries in the results.
     *
     * @since 1.0.1
     */
    IKey[] getKeys(Hive hive, String[] paths) throws RegistryException;

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

    /**
     * Enumerate all the values beneath multiple subkeys of a hive.
     *
     * @since 1.0.1
     */
    IValue[] enumValues(Hive hive, String[] paths) throws RegistryException;

    /**
     * A convenience method for retrieving a string value from the registry.
     *
     * @since 1.0.1
     */
    String getStringValue(Hive hive, String subkey, String value) throws Exception;

    /**
     * Get an ISearchable for the registry.
     *
     * @since 1.0
     */
    ISearchable<IKey> getSearcher();

    /**
     * ISearchable.Condition subclass for IRegistry search conditions.
     *
     * @since 1.2
     */
    public class RegCondition extends Condition {
	/**
	 * Create a new Condition for an IRegistry search.
	 */
	public RegCondition(int type, int field, Object arg) {
	    super(type, field, arg);
	}

	/**
	 * Required search condition field for the hive.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_HIVE = 100;

	/**
	 * Required search condition field for the key path or pattern.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_KEY = 101;

	/**
	 * Search condition field for the value name or pattern.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_VALUE = 102;

	/**
	 * Search condition field for a base-64 encoded value name.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_VALUE_BASE64 = 103;
    }
}
