// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.wmi;

import java.math.BigInteger;

import jsaf.provider.windows.wmi.WmiException;

/**
 * An SWbemProperty interface.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/aa393804(VS.85).aspx">SWbemProperty object</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISWbemProperty {
    /**
     * Get the name of the property.
     *
     * @since 1.0
     */
    String getName() throws WmiException;

    /**
     * Get the value of the property, wrapped by an object provided by the underlying implementation of the WMI provider.
     *
     * @since 1.0
     */
    Object getValue() throws WmiException;

    /**
     * Get the value of the property as an Integer.
     *
     * @since 1.0
     */
    Integer getValueAsInteger() throws WmiException;
    
    /**
     * Get the value of the property as a Long.
     *
     * @since 1.0
     */
    Long getValueAsLong() throws WmiException;

    /**
     * Get the value of the proeprty as a Windows Timestamp, which is the number of 100-nanosecond 'clicks' since 1601AD.
     *
     * @see org.joval.os.windows.Timestamp
     *
     * @since 1.0
     */
    BigInteger getValueAsTimestamp() throws WmiException;

    /**
     * Get the value of the property as a Boolean.
     *
     * @since 1.0
     */
    Boolean getValueAsBoolean() throws WmiException;

    /**
     * Get the value of the property as a String.
     *
     * @since 1.0
     */
    String getValueAsString() throws WmiException;

    /**
     * Get the value of the property as a an array of Strings.
     *
     * @since 1.0
     */
    String[] getValueAsArray() throws WmiException;
}
