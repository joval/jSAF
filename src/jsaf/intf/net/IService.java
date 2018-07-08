// Copyright (C) 2018 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.net;

import java.net.InetSocketAddress;

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

    /**
     * Implementation of IService interface.
     */
    class Entry implements IService, Comparable<IService> {
	private InetSocketAddress addr;
	private Protocol transport;
	private String name;

	public Entry(Protocol transport, InetSocketAddress addr) {
	    this.transport = transport;
	    this.addr = addr;
	    int port = addr.getPort();
	    switch(transport) {
	      case UDP:
		name = PortRegistry.getUdpServiceName(port);
		break;
	      case TCP:
		name = PortRegistry.getTcpServiceName(port);
		break;
	    }
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof Entry) {
		Entry other = (Entry)obj;
		return other.transport == transport &&
		       other.addr.getAddress().getHostAddress().equals(addr.getAddress().getHostAddress()) &&
		       other.addr.getPort() == addr.getPort();
	    }
	    return false;
	}

	@Override
	public int hashCode() {
	    return new StringBuffer()
		.append(Entry.class.getName())
		.append(":")
		.append(toString())
		.toString().hashCode();
	}

	@Override
	public String toString() {
	    return new StringBuffer()
		.append(transport.toString())
		.append(":")
		.append(Bytes.toHexString(addr.getAddress().getAddress()))
		.append(":")
		.append(String.format("0x%04x", addr.getPort()))
		.toString();
	}

	// Implement IService

	public Protocol getProtocol() {
	    return transport;
	}

	public String getName() {
	    return name;
	}

	public InetSocketAddress getSocketAddress() {
	    return addr;
	}

	public int getPort() {
	    return addr.getPort();
	}

	// Implement Comparable<IService>

	public int compareTo(IService other) {
	    return toString().compareTo(other.toString());
	}
    }
}
