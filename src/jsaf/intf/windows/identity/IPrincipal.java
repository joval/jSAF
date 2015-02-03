// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

/**
 * Super-interface for users and groups.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IPrincipal {
    /**
     * An enumeration of Windows principal types.
     *
     * @since 1.0
     */
    public enum Type {
	/**
	 * @since 1.0
	 */
	USER,

	/**
	 * @since 1.0
	 */
	GROUP;
    }

    /**
     * Shortcut for getDomain() + "\\" + getName().
     *
     * @since 1.0
     */
    String getNetbiosName();

    /**
     * Get the domain.
     *
     * @since 1.0
     */
    String getDomain();

    /**
     * Get the name of the user or group.
     *
     * @since 1.0
     */
    String getName();

    /**
     * Get the SID.
     *
     * @since 1.0
     */
    String getSid();

    /**
     * Return the principal type.
     *
     * @since 1.0
     */
    Type getType();
}
