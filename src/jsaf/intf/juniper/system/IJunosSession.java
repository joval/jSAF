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
     * Property indicating the number of milliseconds to wait for a command to begin to return data.
     *
     * NOTE: This overloads the definition of PROP_READ_TIMEOUT inherited from IIosSession.
     */
    String PROP_READ_TIMEOUT = "junos.read.timeout";

    /**
     * Retrieve "request support information" data from the device.
     */
    ISupportInformation getSupportInformation();
}
