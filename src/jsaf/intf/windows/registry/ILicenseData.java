// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.util.Map;

/**
 * Interface to Windows license data.
 *
 * @see <a href="http://www.geoffchappell.com/viewer.htm?doc=studies/windows/km/ntoskrnl/api/ex/slmem/productpolicy.htm">Storage of License Values</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ILicenseData {
    /**
     * An interface describing a license entry.
     *
     * @since 1.0
     */
    public interface IEntry {
	/**
	 * Type constant indicating a String entry.
	 *
	 * @since 1.0
	 */
	int TYPE_SZ	= 1;

	/**
	 * Type constant indicating a binary entry.
	 *
	 * @since 1.0
	 */
	int TYPE_BINARY	= 2;

	/**
	 * Type constant indicating a DWORD entry.
	 *
	 * @since 1.0
	 */
	int TYPE_DWORD	= 4;

	/**
	 * Return the TYPE_* constant corresponding to the entry type.
	 *
	 * @since 1.0
	 */
	int getType();

	/**
	 * Return the name of the entry.
	 *
	 * @since 1.0
	 */
	String getName();

	/**
	 * Get a String representation of the entry.
	 *
	 * @since 1.0
	 */
	String toString();
    }

    /**
     * Interface describing a binary license entry.
     *
     * @since 1.0
     */
    interface IBinaryEntry extends IEntry {
	byte[] getData();
    }

    /**
     * Interface describing a DWORD license entry.
     *
     * @since 1.0
     */
    interface IDwordEntry extends IEntry {
	int getData();
    }

    /**
     * Interface describing a String license entry.
     *
     * @since 1.0
     */
    interface IStringEntry extends IEntry {
	String getData();
    }

    /**
     * Get the license entries.
     *
     * @since 1.0
     */
    Map<String, IEntry> getEntries();
}
