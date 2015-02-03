// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.identity;

/**
 * An exception class for identity management operations.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.1
 */
public class IdentityException extends Exception {
    public IdentityException(String message) {
	super(message);
    }

    public IdentityException(Throwable cause) {
	super(cause);
    }
}
