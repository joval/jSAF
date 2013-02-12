// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.wmi;

import jsaf.provider.windows.wmi.WmiException;

/**
 * An ISWbemObject is a collection of ISWbemProperties.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/aa393741(VS.85).aspx">SWbemObject object</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISWbemObject {
    /**
     * Get the object's property set.
     *
     * @since 1.0
     */
    ISWbemPropertySet getProperties() throws WmiException;
}
