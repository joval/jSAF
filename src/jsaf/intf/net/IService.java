// Copyright (C) 2018 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.net;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.TreeSet;

import jsaf.service.PortRegistry;
import jsaf.util.Bytes;

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
     * Enumeration of supported transport protocols.
     */
    enum Transport {
	TCP,
	UDP;
    }

    /**
     * Enumeration of supported service protocols.
     */
    enum Protocol {
	HTTP,
	FTP,
	SMTP,
	SSH,
	TLS;
    }

    /**
     * Get the transport protocol associated with this port instance.
     */
    Transport getTransport();

    /**
     * Get the service protocol(s) associated with this port instance.
     */
    Collection<Protocol> getProtocols();

    /**
     * Get the name commonly associated with the service, e.g., http.
     *
     * @see <a href="https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml">IANA</a>
     */
    String getName();

    /**
     * Get the socket address for the service entry.
     *
     * Note, the InetAddress associated with the socket address describes the network interface from the perspective of the
     * machine on which the service is running (to the extent that can be determined). For example, a listener bound to all
     * the machine's network interfaces will have a socket address with an IPv4 component of 0.0.0.0.
     */
    InetSocketAddress getSocketAddress();

    /**
     * Shortcut for getSocketAddress().getPort()
     */
    int getPort();
}
