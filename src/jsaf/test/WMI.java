// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.net.UnknownHostException;
import java.util.Iterator;

import jsaf.intf.system.ISession;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.intf.windows.wmi.ISWbemObject;
import jsaf.intf.windows.wmi.ISWbemObjectSet;
import jsaf.intf.windows.wmi.ISWbemProperty;
import jsaf.intf.windows.wmi.ISWbemPropertySet;
import jsaf.provider.windows.wmi.WmiException;

public class WMI {
    IWindowsSession session;

    public WMI(ISession session) {
	this.session = (IWindowsSession)session;
    }

    public synchronized void test(String ns, String wql) {
	try {
	    IWmiProvider provider = session.getWmiProvider();
	    ISWbemObjectSet objSet = provider.execQuery(ns, wql);
	    System.out.println("Objects: " + objSet.getSize());
	    Iterator <ISWbemObject>iter = objSet.iterator();
	    while (iter.hasNext()) {
		ISWbemObject obj = iter.next();
		System.out.println("Object");
		ISWbemPropertySet props = obj.getProperties();
		Iterator <ISWbemProperty>propIter = props.iterator();
		while (propIter.hasNext()) {
		    ISWbemProperty prop = propIter.next();
		    String clazz = prop.getValue() == null ? "null" : prop.getValue().getClass().getName();
		    System.out.println("  " + prop.getName() + "=" + prop.getValue() + ", Class: " + clazz);
		}
	    }
	} catch (WmiException e) {
	    e.printStackTrace();
	}
    }
}
