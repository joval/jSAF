// Copyright (C) 2018 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.net;

import java.util.Collection;

import jsaf.intf.system.ISession;

/**
 * An interface describing a machine that hosts network services (i.e., /etc/services).
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.4
 */
public interface IServiceHost extends ISession {
    /**
     * Returns a collection of IService instances listening on the host.
     */
    Collection<IService> getServices() throws Exception;
}
