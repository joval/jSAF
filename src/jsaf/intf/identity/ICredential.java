// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.identity;

/**
 * A representation of an abstract credential.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ICredential {
    String getUsername();
    String getPassword();
}
