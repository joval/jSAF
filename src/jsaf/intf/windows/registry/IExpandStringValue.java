// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import jsaf.intf.system.IEnvironment;

/**
 * Interface to a Windows registry REG_EXPAND_SZ value.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IExpandStringValue extends IValue {
    /**
     * Get the raw data (variable references are unexpanded).
     */
    public String getData();

    /**
     * Get the fully-expanded version of the data.
     */
    public String getExpandedData(IEnvironment env);
}
