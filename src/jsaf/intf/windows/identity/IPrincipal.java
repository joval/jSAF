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
    public enum Type {
	USER, GROUP;
    }

    public String getNetbiosName();

    public String getDomain();

    public String getName();

    public String getSid();

    public Type getType();

    public boolean isBuiltin();
}
