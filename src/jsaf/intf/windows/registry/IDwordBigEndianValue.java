// Copyright (C) 2015 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.math.BigInteger;

/**
 * Interface to a Windows registry DWORD and DWORD_BIG_ENDIAN values.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.1
 */
public interface IDwordBigEndianValue extends IValue {
    /**
     * Get the data.
     */
    BigInteger getData();
}
