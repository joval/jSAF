// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.net;

import java.net.InetSocketAddress;

/**
 * An interface encapsulating information about a network service (i.e., a listening socket) on a computer system.
 * See: /etc/services
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.4
 */
public interface IService {
    /**
     * Enumeration of possible transport protocols.
     */
    enum Protocol {
	UDP, TCP;
    }

    /**
     * Get the transport protocol associated with the service.
     */
    Protocol getProtocol();

    /**
     * Get the name associated with the service, e.g., http.
     *
     * @see <a href="https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml">IANA</a>
     */
    String getName();

    /**
     * Get the socket address for the service entry. Note, the InetAddress associated with the socket address describes the
     * network interface from the perspective of the machine on which the service is running.
     */
    InetSocketAddress getSocketAddress();

    /**
     * Shortcut for getSocketAddress().getPort()
     */
    int getPort();
}
