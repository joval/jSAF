// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.cisco.system;

import jsaf.intf.netconf.INetconf;

/**
 * A representation of an NX-OS command-line session.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.5.1
 */
public interface INexusSession extends ICiscoSession {
    /**
     * Get an INetconf for the device.
     */
    INetconf getNetconf();
}
