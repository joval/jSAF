// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jsaf.intf.windows.registry.IBinaryValue;
import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.ILicenseData;
import jsaf.intf.windows.registry.ILicenseData.IBinaryEntry;
import jsaf.intf.windows.registry.ILicenseData.IDwordEntry;
import jsaf.intf.windows.registry.ILicenseData.IEntry;
import jsaf.intf.windows.registry.ILicenseData.IStringEntry;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.registry.IValue;
import jsaf.io.LittleEndian;

/**
 * A class that can interpret a License registry key.
 * See http://www.geoffchappell.com/viewer.htm?doc=studies/windows/km/ntoskrnl/api/ex/slmem/productpolicy.htm
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class LicenseData implements ILicenseData {
    private Map<String, IEntry> entries;

    public LicenseData(IRegistry reg) throws Exception {
	entries = new HashMap<String, IEntry>();
	IKey key = reg.getKey(IRegistry.Hive.HKLM, "SYSTEM\\CurrentControlSet\\Control\\ProductOptions");
	IValue value = key.getValue("ProductPolicy");
	switch(value.getType()) {
	  case REG_BINARY: {
	    byte[] buff = ((IBinaryValue)value).getData();
	    //
	    // Read the header
	    //
	    int len		= LittleEndian.getUInt(buff, 0x00);
	    int valueLen	= LittleEndian.getUInt(buff, 0x04);
	    int endSize		= LittleEndian.getUInt(buff, 0x08);
	    int junk		= LittleEndian.getUInt(buff, 0x0C);
	    int version		= LittleEndian.getUInt(buff, 0x10);

	    if (version == 1) {
		if (len == buff.length) {
		    int offset = 0x14;
		    for (int bytesRead=0; bytesRead < valueLen; ) {
			IEntry entry = readEntry(buff, offset);
			entries.put(entry.getName(), entry);
			bytesRead = bytesRead + entry.length();
			offset = offset + entry.length();
		    }
		} else {
		    throw new RuntimeException("Unexpected buffer length: " + buff.length + " (" + len + " expected)");
		}
	    } else {
		throw new RuntimeException("Unexpected version number: " + version);
	    }
	    break;
	  }

	  default:
	    throw new RuntimeException("Unexpected type: " + value.getType());
	}
    }

    public Map<String, IEntry> getEntries() {
	return entries;
    }

    // Private

    private IEntry readEntry(byte[] buff, int offset) throws Exception {
	short len	= LittleEndian.getUShort(buff, offset);
	short nameLen	= LittleEndian.getUShort(buff, offset + 0x02);
	short dataType	= LittleEndian.getUShort(buff, offset + 0x04);
	short dataLen	= LittleEndian.getUShort(buff, offset + 0x06);
	int flags	= LittleEndian.getUInt(buff, offset + 0x08);
	int padding	= LittleEndian.getUInt(buff, offset + 0x0C);
	String name	= LittleEndian.getSzUTF16LEString(buff, offset + 0x10, (int)nameLen);
	byte[] data	= Arrays.copyOfRange(buff, offset + 0x10 + nameLen, offset + 0x10 + nameLen + dataLen);

	switch(dataType) {
	  case IEntry.TYPE_DWORD:
	    if (dataLen == 4) {
		return new DwordEntry(len, dataType, name, data);
	    } else {
		throw new RuntimeException("Illegal length for DWORD data: " + dataLen);
	    }

	  case IEntry.TYPE_SZ:
	    return new StringEntry(len, dataType, name, data);

	  case IEntry.TYPE_BINARY:
	  default:
	    return new BinaryEntry(len, dataType, name, data);
	}
    }

    abstract class Entry implements IEntry {
	int dataType;
	String name;

	Entry(int dataType, String name) {
	    this.dataType = dataType;
	    this.name = name;
	}

	public int getType() {
	    return dataType;
	}

	public String getName() {
	    return name;
	}
    }

    class BinaryEntry extends Entry implements IBinaryEntry {
	private byte[] data;

	BinaryEntry(int dataType, String name, byte[] data) {
	    super(dataType, name);
	    this.data = data;
	}

	public byte[] getData() {
	    return data;
	}

	public String toString() {
	    StringBuffer sb = new StringBuffer(name);
	    sb.append(": BINARY: ");
	    for (int i=0; i < data.length; i++) {
		sb.append(LittleEndian.toHexString(data[i]));
	    }
	    return sb.toString();
	}
    }

    class DwordEntry extends Entry implements IDwordEntry {
	private int data;

	DwordEntry(int dataType, String name, byte[] data) {
	    super(dataType, name);
	    this.data = LittleEndian.getUInt(data, 0);
	}

	public int getData() {
	    return data;
	}

	public String toString() {
	    return name + ": DWORD: " + Integer.toHexString(data);
	}
    }

    class StringEntry extends Entry implements IStringEntry {
	private String data;

	StringEntry(int dataType, String name, byte[] data) {
	    super(dataType, name);
	    this.data = LittleEndian.getSzUTF16LEString(data, 0, data.length);
	}

	public String getData() {
	    return data;
	}

	public String toString() {
	    return name + ": String: " + data;
	}
    }
}
