// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.wmi.scripting;

import java.util.Iterator;
import java.util.Vector;

import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;

import jsaf.intf.windows.wmi.ISWbemObject;
import jsaf.intf.windows.wmi.ISWbemObjectSet;
import jsaf.provider.windows.wmi.WmiException;

/**
 * Wrapper for an SWbemObjectSet.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SWbemObjectSet implements ISWbemObjectSet {
    Dispatch dispatch;
    Vector<ISWbemObject> objects;

    public SWbemObjectSet(Dispatch dispatch) {
	this.dispatch = dispatch;
	EnumVariant enumVariant = new EnumVariant(dispatch);
	objects = new Vector<ISWbemObject>();
	try {
	    while(enumVariant.hasMoreElements()) {
		objects.add(new SWbemObject(enumVariant.nextElement().toDispatch()));
	    }
	} catch (Exception e) {
	}
    }

    // Implement ISWbemObjectSet

    public Iterator<ISWbemObject> iterator() {
	return objects.iterator();
    }

    public int getSize() {
	return objects.size();
    }
}
