// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

/**
 * Exception class for the Windows registry.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class RegistryException extends Exception {
    public RegistryException() {
	super();
    }

    public RegistryException(String message) {
	super(message);
    }

    public RegistryException(Exception cause) {
	super(cause);
    }
}
