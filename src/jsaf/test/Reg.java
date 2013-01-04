// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.*;

import jsaf.intf.system.IBaseSession;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.IValue;
import jsaf.intf.windows.registry.IBinaryValue;
import jsaf.intf.windows.registry.IDwordValue;
import jsaf.intf.windows.registry.IExpandStringValue;
import jsaf.intf.windows.registry.ILicenseData;
import jsaf.intf.windows.registry.ILicenseData.IEntry;
import jsaf.intf.windows.registry.IMultiStringValue;
import jsaf.intf.windows.registry.IStringValue;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.io.LittleEndian;

public class Reg {
    IWindowsSession session;

    public Reg(IBaseSession session) {
	if (session instanceof IWindowsSession) {
	    this.session = (IWindowsSession)session;
	}
    }

    public void testLicense() {
	try {
	    IRegistry r = session.getRegistry(IWindowsSession.View._64BIT);
	    Hashtable<String, IEntry> ht = r.getLicenseData().getEntries();
	    for (IEntry entry : ht.values()) {
		System.out.println(entry.toString());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void test(String keyName, String valueName) {
	try {
	    IRegistry r = session.getRegistry(IWindowsSession.View._64BIT);
	    IKey key = r.getKey(keyName);

	    if (valueName == null) {
		String[] sa = key.listSubkeys();
		System.out.println("Subkeys: " + sa.length);
		for (int i=0; i < sa.length; i++) {
		    System.out.println("  Subkey name: " + sa[i]);
		}
		IValue[] values = key.listValues();
		System.out.println("Values: " + values.length);
		for (int i=0; i < values.length; i++) {
		    System.out.println("  Value: " + values[i].toString());
		}
	    } else {
		System.out.println(key.getValue(valueName).toString());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

