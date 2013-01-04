// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.IBinaryValue;
import jsaf.util.Base64;

/**
 * Representation of a Windows registry binary value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class BinaryValue extends Value implements IBinaryValue {
    private byte[] data;

    public BinaryValue(IKey parent, String name, byte[] data) {
	type = Type.REG_BINARY;
	this.parent = parent;
	this.name = name;
	this.data = data;
    }

    public byte[] getData() {
	return data;
    }

    public String toString() {
	return "BinaryValue [Name=\"" + name + "\", Value={" + Base64.encodeBytes(data) + "}]";
    }
}
