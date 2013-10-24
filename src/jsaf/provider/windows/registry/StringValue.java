// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.IStringValue;

/**
 * Representation of a Windows registry string value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class StringValue extends Value implements IStringValue {
    private static final char NULL = 0;

    private String data;

    public StringValue(IKey parent, String name, String data) {
	type = Type.REG_SZ;
	this.parent = parent;
	this.name = name;

	//
	// Just in case a stray null appears at the end, clip it off
	//
	int len = data.length();
	if (len > 0 && data.charAt(len-1) == NULL) {
	    this.data = data.substring(0,len-1);
	} else {
	    this.data = data;
	}
    }

    public String getData() {
	return data;
    }

    public String toString() {
	return "StringValue [Name=\"" + name + "\" Value=\"" + data + "\"]";
    }
}
