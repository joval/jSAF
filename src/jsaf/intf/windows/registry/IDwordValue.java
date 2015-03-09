// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.math.BigInteger;

/**
 * Interface to a Windows registry DWORD and DWORD_BIG_ENDIAN values.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IDwordValue extends IValue {
    /**
     * Get the data.
     *
     * @since 1.0
     */
    BigInteger getData();
}
