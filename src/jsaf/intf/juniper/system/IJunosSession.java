// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.juniper.system;

import jsaf.intf.netconf.INetconf;

/**
 * A representation of a JunOS command-line session.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IJunosSession extends INetconf {
    /**
     * Retrieve "request support information" data from the device.
     */
    ISupportInformation getSupportInformation();
}
