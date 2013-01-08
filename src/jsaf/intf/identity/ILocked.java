// Copyright (C) 2011 jOVAL.org.  All rights reserved.

package jsaf.intf.identity;

/**
 * Something that requires a credential for access.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ILocked {
    public boolean unlock(ICredential cred);
}
