// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.juniper.system;

import jsaf.intf.system.IComputerSystem;
import jsaf.intf.netconf.INetconf;

/**
 * A representation of a JunOS command-line session.
 *
 * @see jsaf.intf.system.IComputerSystem
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IJunosSession extends IComputerSystem {
    /**
     * Retrieve "request support information" data from the device.
     *
     * @since 1.0
     */
    ISupportInformation getSupportInformation();

    /**
     * Get the INetconf for this device.
     *
     * @since 1.3.1
     */
    INetconf getNetconf();
}
