// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.wmi;

import java.util.Iterator;

import jsaf.provider.windows.wmi.WmiException;

/**
 * An SWbemObjectSet object is a collection of SWbemObject objects.
 * 
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISWbemObjectSet extends Iterable<ISWbemObject> {
    /**
     * Iterate over the objects in the set.
     *
     * @since 1.0
     */
    Iterator<ISWbemObject> iterator();

    /**
     * Get the number of objects in the set.
     *
     * @since 1.0
     */
    int getSize();
}
