// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

/**
 * Super-interface for users and groups.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IPrincipal {
    /**
     * An enumeration of Windows principal types.
     */
    public enum Type {
	USER, GROUP;
    }

    /**
     * Shortcut for getDomain() + "\\" + getName().
     */
    public String getNetbiosName();

    /**
     * Get the domain.
     */
    public String getDomain();

    /**
     * Get the name of the user or group.
     */
    public String getName();

    /**
     * Get the SID.
     */
    public String getSid();

    /**
     * Return the principal type.
     */
    public Type getType();

    /**
     * Is the SID well-known?
     *
     * @see <a href="http://support.microsoft.com/kb/243330?wa=wsignin1.0">KB243330</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/cc980032%28v=prot.20%29.aspx">Well-Known SID Structures</a>
     */
    public boolean isBuiltin();
}
