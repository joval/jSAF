// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.math.BigInteger;

/**
 * Interface to a Windows registry DWORD value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IDwordValue extends IValue {
    /**
     * Get the data.
     */
    public BigInteger getData();
}
