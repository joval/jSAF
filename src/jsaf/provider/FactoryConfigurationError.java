// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

/**
 * An exception indicating there is a problem with the configuration of the SessionFactory.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class FactoryConfigurationError extends Error {
    public FactoryConfigurationError(String message) {
	super(message);
    }

    public FactoryConfigurationError(Exception e) {
	super(e);
    }
}
