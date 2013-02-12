// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.wmi;

import jsaf.provider.windows.wmi.WmiException;

/**
 * An ISWbemEventSource is a source of ISWbemObjects.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/aa393710%28v=vs.85%29.aspx">SWbemEventSource object</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISWbemEventSource {
    /**
     * Get the object's property set.
     *
     * @since 1.0
     */
    ISWbemObject nextEvent() throws WmiException;
}
