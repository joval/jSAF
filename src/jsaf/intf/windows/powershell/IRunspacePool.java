// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.powershell;

import jsaf.intf.windows.system.IWindowsSession;

/**
 * An interface to a Powershell runspace pool.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRunspacePool {
    /**
     * Get an IRunspace from the pool, with the IWindowsSession's default architecture. If a lock cannot be obtained
     * on an existing runspace, a new one will be spawned and returned.
     *
     * @throws IndexOutOfBoundsException if the pool is already at capacity.
     *
     * @since 1.1.2
     */
    IRunspace getRunspace() throws Exception;

    /**
     * Get an IRunspace from the pool, with the specified architecture. If a lock cannot be obtained on an existing
     * runspace, a new one will be spawned and returned.
     *
     * @throws IndexOutOfBoundsException if the pool is already at capacity.
     *
     * @since 1.1.2
     */
    IRunspace getRunspace(IWindowsSession.View view) throws Exception;
}
