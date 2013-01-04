// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import java.math.BigInteger;

import jsaf.intf.windows.registry.IDwordValue;
import jsaf.intf.windows.registry.IKey;
import jsaf.io.LittleEndian;

/**
 * Representation of a Windows registry DWORD value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class DwordValue extends Value implements IDwordValue {
    protected BigInteger data;

    public DwordValue(IKey parent, String name, BigInteger data) {
	type = Type.REG_DWORD;
	this.parent = parent;
	this.name = name;
	this.data = data;
    }

    public BigInteger getData() {
	return data;
    }

    public String toString() {
	return "DwordValue [Name=\"" + name + "\", Value=0x" + String.format("%08x", data.intValue()) + "]";
    }
}
