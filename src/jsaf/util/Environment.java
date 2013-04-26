// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.util.Properties;

import jsaf.intf.system.IEnvironment;

/**
 * An IEnvironment implementation that can be initialized with java.util.Properties or another IEnvironment.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Environment extends AbstractEnvironment {
    /**
     * Create from properties (case-sensitive).
     */
    public Environment(Properties props) {
	this(props, false);
    }

    public Environment(Properties props, boolean caseInsensitive) {
	super(caseInsensitive);
	this.props = props;
    }

    /**
     * Set a variable using a string of the form "variable=value". If no value is specified, the variable will be unset.
     */
    public void setenv(String pair) {
	int ptr = pair.indexOf("=");
	if (ptr == -1) {
	    throw new IllegalArgumentException(pair);
	} else {
	    setenv(pair.substring(0,ptr), pair.substring(ptr+1));
	}
    }

    /**
     * Set a variable value. Use a value of null or an empty string to unset a value.
     */
    public void setenv(String variable, String value) {
	if (caseInsensitive) {
	    for (String key : this) {
		if (key.equalsIgnoreCase(variable)) {
		    variable = key;
		    break;
		}
	    }
	}
	if (value == null || "".equals(value)) {
	    props.remove(variable);
	} else {
	    props.setProperty(variable, value);
	}
    }
}
