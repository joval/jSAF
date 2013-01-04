// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import java.math.BigInteger;

import jsaf.intf.windows.registry.IQwordValue;
import jsaf.intf.windows.registry.IKey;
import jsaf.io.LittleEndian;

/**
 * Representation of a Windows registry QWORD value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class QwordValue extends Value implements IQwordValue {
    private BigInteger data;

    public QwordValue(IKey parent, String name, BigInteger data) {
	type = Type.REG_QWORD;
	this.parent = parent;
	this.name = name;
	this.data = data;
    }

    public BigInteger getData() {
	return data;
    }

    public String toString() {
	return "QwordValue [Name=\"" + name + "\", Value=0x" + String.format("%016x", data.longValue()) + "]";
    }
}
