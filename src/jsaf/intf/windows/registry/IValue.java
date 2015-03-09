// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import jsaf.provider.windows.registry.RegistryException;

/**
 * Interface to an abstract Windows registry value.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IValue {
    /**
     * An enumeration of registry value types.
     *
     * @since 1.0
     */
    enum Type {
	/**
	 * Type for values containing no data at all.
	 *
	 * @since 1.0
	 */
	REG_NONE("REG_NONE", 0),

	/**
	 * Type for string values.
	 *
	 * @since 1.0
	 */
	REG_SZ("REG_SZ", 1),

	/**
	 * Type for expandable string values.
	 *
	 * @since 1.0
	 */
	REG_EXPAND_SZ("REG_EXPAND_SZ", 2),

	/**
	 * Type for binary values.
	 *
	 * @since 1.0
	 */
	REG_BINARY("REG_BINARY", 3),

	/**
	 * Type for 32-bit (little-endian) integer values.
	 *
	 * @since 1.0
	 */
	REG_DWORD("REG_DWORD", 4),

	/**
	 * Type for 32-bit bit-endian integer values.
	 *
	 * @since 1.3.1
	 */
	REG_DWORD_BIG_ENDIAN("REG_DWORD_BIG_ENDIAN", 5),

	/**
	 * Type for registry links.
	 *
	 * @since 1.3.1
	 */
	REG_LINK("REG_LINK", 6),

	/**
	 * Type for string array values.
	 *
	 * @since 1.0
	 */
	REG_MULTI_SZ("REG_MULTI_SZ", 7),

	/**
	 * Type for registry resource lists.
	 *
	 * @since 1.3.1
	 */
	REG_RESOURCE_LIST("REG_RESOURCE_LIST", 8),

	/**
	 * Type for registry resource descriptors.
	 *
	 * @since 1.3.1
	 */
	REG_FULL_RESOURCE_DESCRIPTOR("REG_FULL_RESOURCE_DESCRIPTOR", 9),

	/**
	 * Type for registry resource requirements lists.
	 *
	 * @since 1.3.1
	 */
	REG_RESOURCE_REQUIREMENTS_LIST("REG_RESOURCE_REQUIREMENTS_LIST", 10),

	/**
	 * Type for 64-bit (little-endian) integer values.
	 *
	 * @since 1.0
	 */
	REG_QWORD("REG_QWORD", 11);

	private String name;
	private int id;

	private Type(String name, int id) {
	    this.name = name;
	    this.id = id;
	}

	/**
	 * Get the "REG_*" name of the Type.
	 *
	 * @since 1.0
	 */
	public String getName() {
	    return name;
	}

	/**
	 * Return the ID of the Type.
	 *
	 * @since 1.0
	 */
	public int getId() {	
	    return id;
	}

	/**
	 * Return the Type corresponding to the String name.
	 *
	 * @since 1.0
	 */
	public static Type fromName(String name) throws IllegalArgumentException {
	    for (Type type : values()) {
		if (type.getName().equals(name)) {
		    return type;
		}
	    }
	    throw new IllegalArgumentException(name);
	}

	/**
	 * Return the Type corresponding to the specified ID.
	 *
	 * @since 1.3.1
	 */
	public static Type fromId(int id) throws IllegalArgumentException {
	    for (Type type : values()) {
		if (type.getId() == id) {
		    return type;
		}
	    }
	    throw new IllegalArgumentException(Integer.toString(id));
	}
    }

    /**
     * Returns the corresponding REG_ constant.
     *
     * @since 1.0
     */
    Type getType() throws RegistryException;

    /**
     * Return the Key under which this Value lies.
     *
     * @since 1.0
     */
    IKey getKey();

    /**
     * Return the Value's name.
     *
     * @since 1.0
     */
    String getName() throws RegistryException;

    /**
     * Returns a String suitable for logging about the Value.
     *
     * @since 1.0
     */
    String toString();
}
