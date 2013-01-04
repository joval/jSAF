// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import jsaf.intf.windows.registry.INoneValue;
import jsaf.intf.windows.registry.IKey;

/**
 * Representation of a Windows registry NONE value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class NoneValue extends Value implements INoneValue {
    public NoneValue(IKey parent, String name) {
	type = Type.REG_NONE;
	this.parent = parent;
	this.name = name;
    }

    public String toString() {
	return "NoneValue [Name=\"" + name + "\"]";
    }
}
