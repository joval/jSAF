// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.wmi.scripting;

import java.util.ArrayList;
import java.util.Iterator;

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
    ArrayList<ISWbemObject> objects;

    public SWbemObjectSet(Dispatch dispatch) throws Exception {
	this.dispatch = dispatch;
	EnumVariant enumVariant = new EnumVariant(dispatch);
	objects = new ArrayList<ISWbemObject>();
	while(enumVariant.hasMoreElements()) {
	    objects.add(new SWbemObject(enumVariant.nextElement().toDispatch()));
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
