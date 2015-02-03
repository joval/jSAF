// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.service;

public class UnsupportedServiceException extends Exception {
    public UnsupportedServiceException(String message) {
	super(message);
    }

    public UnsupportedServiceException(Exception cause) {
	super(cause);
    }
}
