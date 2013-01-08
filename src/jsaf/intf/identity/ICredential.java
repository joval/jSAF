// Copyright (C) 2011 jOVAL.org.  All rights reserved.

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
