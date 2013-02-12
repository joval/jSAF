// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

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
	REG_NONE("REG_NONE", "Unknown", 0),

	/**
	 * Type for 32-bit integer values.
	 *
	 * @since 1.0
	 */
	REG_DWORD("REG_DWORD", "DWord", 1),

	/**
	 * Type for binary values.
	 *
	 * @since 1.0
	 */
	REG_BINARY("REG_BINARY", "Binary", 2),

	/**
	 * Type for string values.
	 *
	 * @since 1.0
	 */
	REG_SZ("REG_SZ", "String", 3),

	/**
	 * Type for expandable string values.
	 *
	 * @since 1.0
	 */
	REG_EXPAND_SZ("REG_EXPAND_SZ", "ExpandString", 4),

	/**
	 * Type for string array values.
	 *
	 * @since 1.0
	 */
	REG_MULTI_SZ("REG_MULTI_SZ", "MultiString", 5),

	/**
	 * Type for 64-bit integer values.
	 *
	 * @since 1.0
	 */
	REG_QWORD("REG_QWORD", "QWord", 6);

	private String name, kind;
	private int id;

	private Type(String name, String kind, int id) {
	    this.name = name;
	    this.kind = kind;
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
	 * The "Kind" as returned by C# Microsoft.Win32.RegistryKey::GetValueKind
	 *
	 * @since 1.0
	 */
	public String getKind() {
	    return kind;
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
	 * Return the Type corresponding to the String kind.
	 *
	 * @since 1.0
	 */
	public static Type fromKind(String kind) throws IllegalArgumentException {
	    for (Type type : values()) {
		if (type.getKind().equals(kind)) {
		    return type;
		}
	    }
	    throw new IllegalArgumentException(kind);
	}
    }

    /**
     * Returns the corresponding REG_ constant.
     *
     * @since 1.0
     */
    Type getType();

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
    String getName();

    /**
     * Returns a String suitable for logging about the Value.
     *
     * @since 1.0
     */
    String toString();
}
