// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.wmi.scripting;

import com.jacob.com.Dispatch;

import jsaf.intf.windows.wmi.ISWbemObject;
import jsaf.intf.windows.wmi.ISWbemObjectSet;
import jsaf.intf.windows.wmi.ISWbemProperty;
import jsaf.intf.windows.wmi.ISWbemPropertySet;
import jsaf.provider.windows.wmi.WmiException;

/**
 * Wrapper for an SWbemObject.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SWbemObject implements ISWbemObject {
    private Dispatch dispatch;

    SWbemObject(Dispatch dispatch) {
	this.dispatch = dispatch;
    }

    // Implement ISWbemObject

    public ISWbemPropertySet getProperties() throws WmiException {
	return new SWbemPropertySet(Dispatch.call(dispatch, "Properties_").toDispatch());
    }
}
