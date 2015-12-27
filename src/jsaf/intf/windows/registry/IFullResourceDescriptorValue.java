// Copyright (C) 2015 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.util.List;

/**
 * Interface to a Windows registry resource descriptor.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.1
 * @see <a href="https://msdn.microsoft.com/en-us/library/windows/hardware/ff541954(v=vs.85).aspx">CM_FULL_RESOURCE_DESCRIPTOR structure</a>
 */
public interface IFullResourceDescriptorValue extends IValue {
    /**
     * Get the raw data.
     */
    byte[] getData();
}
