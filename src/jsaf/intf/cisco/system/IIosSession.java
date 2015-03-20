// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.cisco.system;

import jsaf.intf.netconf.INetconf;

/**
 * A representation of an IOS command-line session.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IIosSession extends ICiscoSession {
    /**
     * Get an INetconf for the device.
     *
     * @since 1.3.1
     */
    INetconf getNetconf();
}
