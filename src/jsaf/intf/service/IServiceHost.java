// Copyright (C) 2018 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.service;

import java.util.Collection;

import jsaf.intf.net.IService;

/**
 * An interface describing a machine that hosts network services (i.e., /etc/services).
 *
 * Classes implementing ISession may optionally implement this IServiceHost interface as well.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.4
 */
public interface IServiceHost {
    /**
     * Property indicating the maximum number of milliseconds to wait for a TCP connection attempt to complete when performing a
     * port scan of a device over the network.
     */
    String PROP_PORTSCAN_TIMEOUT = "portScan.timeout";

    /**
     * Returns a collection of IService instances running on the host.
     */
    Collection<IService> getServices() throws Exception;
}
