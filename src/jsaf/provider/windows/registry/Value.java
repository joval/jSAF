// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.IValue;

/**
 * Abstract base class representing a Windows registry value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public abstract class Value implements IValue {
    protected Type type = Type.REG_NONE;
    protected String name = null;
    protected IKey parent = null;

    // Implement IValue

    public final Type getType() {
	return type;
    }

    public final IKey getKey() {
	return parent;
    }

    public final String getName() {
	return name;
    }

    public abstract String toString();
}
