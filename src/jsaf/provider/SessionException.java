// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

/**
 * An exception indicating there is a problem with an ISession.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class SessionException extends RuntimeException {
    public SessionException() {
	super();
    }

    public SessionException(String message) {
	super(message);
    }

    public SessionException(Throwable cause) {
	super(cause);
    }

    public SessionException(String message, Throwable cause) {
	super(message, cause);
    }

    @Override
    public String getMessage() {
	String s = super.getMessage();
	if (s == null) {
	    return getMessage(getCause());
	} else {
	    return s;
	}
    }

    // Private

    private String getMessage(Throwable t) {
	if (t == null) {
	    return null;
	} else if (t.getMessage() == null) {
	    String s = getMessage(t.getCause());
	    if (s == null) {
		return t.getClass().getName();
	    } else {
		return s;
	    }
	} else {
	    return t.getMessage();
	}
    }
}
