// Copyright (C) 2015 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

/**
 * Interface to a Windows registry REG_SZ value.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.1
 */
public interface ILinkValue extends IValue {
    /**
     * Get the data.
     */
    public String getData();
}
