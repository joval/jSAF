// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.powershell;

/**
 * An exception class for Powershell operations.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class PowershellException extends Exception {
    public PowershellException(String message) {
	super(message);
    }

    public PowershellException(Throwable cause) {
	super(cause);
    }

    public PowershellException(String message, Throwable cause) {
	super(message, cause);
    }
}
