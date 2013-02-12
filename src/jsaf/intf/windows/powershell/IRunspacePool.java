// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.powershell;

import java.util.Collection;
import java.util.NoSuchElementException;

import jsaf.intf.windows.system.IWindowsSession;

/**
 * An interface to a powershell runspace pool.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRunspacePool {
    /**
     * Returns all the currently open runspaces.
     *
     * @since 1.0
     */
    Collection<IRunspace> enumerate();

    /**
     * Returns the maximum number of runspaces that can live in the pool.
     *
     * @since 1.0
     */
    int capacity();

    /**
     * Get a specific runspace, given its ID.
     *
     * @throws NoSuchElementException if no runspace with the given ID was found in the pool.
     *
     * @since 1.0
     */
    IRunspace get(String id) throws NoSuchElementException;

    /**
     * Create (and return) a new Runspace in the pool (default architecture).
     *
     * @throws IndexOutOfBoundsException if the pool is already at capacity.
     *
     * @since 1.0
     */
    IRunspace spawn() throws Exception;

    /**
     * Create (and return) a new Runspace in the pool for the specified architecture.
     *
     * @throws IndexOutOfBoundsException if the pool is already at capacity.
     *
     * @since 1.0
     */
    IRunspace spawn(IWindowsSession.View view) throws Exception;
}
