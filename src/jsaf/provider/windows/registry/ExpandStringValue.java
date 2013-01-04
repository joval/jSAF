// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import jsaf.intf.windows.registry.IExpandStringValue;
import jsaf.intf.windows.registry.IKey;
import jsaf.intf.system.IEnvironment;

/**
 * Representation of a Windows registry expand-string value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class ExpandStringValue extends Value implements IExpandStringValue {
    private String data;

    public ExpandStringValue(IKey parent, String name, String data) {
	type = Type.REG_EXPAND_SZ;
	this.parent = parent;
	this.name = name;
	this.data = data;
    }

    public String getData() {
	return data;
    }

    /**
     * Get the value with environment variables filled out from the system environment.
     */
    public String getExpandedData(IEnvironment env) {
	return env.expand(data);
    }

    public String toString() {
	return "StringValue [Name=\"" + name + "\" Value=\"" + data + "\"]";
    }
}
