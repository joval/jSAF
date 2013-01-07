// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.system;

import jsaf.intf.system.ISession;
import jsaf.intf.windows.identity.IDirectory;
import jsaf.intf.windows.io.IWindowsFilesystem;
import jsaf.intf.windows.powershell.IRunspacePool;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.wmi.IWmiProvider;

/**
 * A representation of a Windows session.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IWindowsSession extends ISession {
    String ENV_ARCH = "PROCESSOR_ARCHITECTURE";
    String ENV_AR32 = "PROCESSOR_ARCHITEW6432";

    public enum View {
	_32BIT,
	_64BIT;
    }

    View getNativeView();

    IRegistry getRegistry(View view);

    boolean supports(View view);

    /**
     * As an ISession, the getFilesystem() call always returns a non-redirected view.
     */
    IWindowsFilesystem getFilesystem(View view);

    IWmiProvider getWmiProvider();

    IDirectory getDirectory();

    IRunspacePool getRunspacePool();
}
