// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.wmi;

import java.util.NoSuchElementException;

import jsaf.intf.util.ILoggable;
import jsaf.provider.windows.wmi.WmiException;

/**
 * An interface to WMI for performing queries.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IWmiProvider extends ILoggable {
    public static final String CIMv2 = "root\\cimv2";

    /**
     * Execute a WQL query on the given namespace.
     *
     * @throws NoSuchElementException if the specified namespace is not registered
     * @throws WmiException if there was an error performing the query
     */
    public ISWbemObjectSet execQuery(String ns, String wql) throws NoSuchElementException, WmiException;

    /**
     * Execute a notification query.
     *
     * @throws NoSuchElementException if the specified namespace is not registered
     * @throws WmiException if there was an error performing the query
     */
    public ISWbemEventSource execNotificationQuery(String ns, String wql) throws NoSuchElementException, WmiException;
}
