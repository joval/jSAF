// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.system;

import jsaf.intf.system.IComputerSystem;
import jsaf.intf.windows.identity.IDirectory;
import jsaf.intf.windows.io.IWindowsFilesystem;
import jsaf.intf.windows.powershell.IRunspacePool;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.provider.windows.wmi.WmiException;

/**
 * A representation of a Windows session.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IWindowsSession extends IComputerSystem {
    /**
     * Property indicating the number of milliseconds to wait for a WMI query to return, before quitting and throwing an
     * exception.
     *
     * @since 1.2
     */
    String PROP_WMI_TIMEOUT = "wmi.timeout";

    /**
     * Property indicating the default number of milliseconds to wait for output from a Powershell command, before quitting
     * and throwing an exception.
     *
     * @since 1.3.5
     */
    String PROP_POWERSHELL_TIMEOUT = "powershell.timeout";

    /**
     * Property indicating the number of milliseconds to wait for a Powershell process to present a prompt to stdout when
     * initially started, before quitting, killing the process, and throwing an exception.
     *
     * @since 1.4.5
     */
    String PROP_POWERSHELL_INIT_TIMEOUT = "powershell.initTimeout";

    /**
     * Property indicating a pathname pointing to powershell.exe, or a suitable wrapper program.
     *
     * @since 1.6.12
     */
    String PROP_POWERSHELL_PATH = "powershell.path";

    /**
     * Property indicating the initialization arguments to pass to the powershell process upon startup.
     *
     * @since 1.3.6
     */
    String PROP_POWERSHELL_INIT_ARGS = "powershell.initArgs";

    /**
     * Property indicating the value that should be set for %TEMP% and %TMP% when launching Powershell.
     *
     * @since 1.6.8
     */
    String PROP_POWERSHELL_TMPDIR = "powershell.tmpdir";

    /**
     * Property indicating whether to enable Powershell process memory profiling. When enabled, implementations should log
     * the Powershell process memory size after every call to IRunspace.invoke.
     *
     * @since 1.4.1
     */
    String PROP_POWERSHELL_MEMORY_PROFILING = "powershell.memoryProfiling";

    /**
     * Property governing the maximum single-threaded CPU utilization for a filesystem search.
     *
     * @since 1.6.0
     */
    String PROP_FS_SEARCH_MAX_CPU = "fs.search.maxCpu";

    /**
     * Property governing the maximum time that can be used for any one filesystem search.
     *
     * @since 1.6.6
     */
    String PROP_FS_SEARCH_TIMEOUT = "fs.search.timeout";

    /**
     * Property indicating the maximum single-threaded CPU utilization for a registry search.
     *
     * @since 1.6.0
     */
    String PROP_REGISTRY_SEARCH_MAX_CPU = "registry.search.maxCpu";

    /**
     * Property governing the maximum time that can be used for any one registry search.
     *
     * @since 1.6.6
     */
    String PROP_REGISTRY_SEARCH_TIMEOUT = "registry.search.timeout";

    /**
     * Name of the environment variable containing the processor architecture (when running in 32-bit mode on a 64-bit
     * machine, the value will actually be the emulated architecture).
     *
     * @since 1.0
     */
    String ENV_ARCH = "PROCESSOR_ARCHITECTURE";

    /**
     * Name of the environment variable containing the actual processor architecture, when running in 32-bit mode on a
     * 64-bit machine.
     *
     * @since 1.0
     */
    String ENV_AR32 = "PROCESSOR_ARCHITEW6432";

    /**
     * SID for the Administrators group.
     *
     * @since 1.1
     */
    String ADMINISTRATORS_SID = "S-1-5-32-544";

    /**
     * An enumeration of possible views.
     *
     * @since 1.0
     */
    public enum View {
	/**
	 * 32-bit view.
	 *
	 * @since 1.0
	 */
	_32BIT,

	/**
	 * 64-bit view.
	 *
	 * @since 1.0
	 */
	_64BIT;
    }

    /**
     * Get the native view of the session.
     *
     * @since 1.0
     */
    View getNativeView();

    /**
     * Obtain an IRegistry for the machine. For a 32-bit machine, the view is ignored.
     *
     * @since 1.0
     */
    IRegistry getRegistry(View view);

    /**
     * Test whether the session supports the specified view.
     *
     * @since 1.0
     */
    boolean supports(View view);

    /**
     * Test whether the session has the ability to run commands requiring UAC privileges.
     *
     * @since 1.3
     */
    boolean privileged();

    /**
     * As an IComputerSystem, the getFilesystem() call always returns a non-redirected view, i.e.,
     * getFilesystem(getNativeView()). This method facilitates access to the 32-bit view on a 64-bit machine.
     * (For a 32-bit machine, the view is ignored).
     *
     * @since 1.0
     */
    IWindowsFilesystem getFilesystem(View view);

    /**
     * Obtain a WMI provider for the machine. (This should always be a WMI provider for the native processor architecture).
     *
     * @since 1.0
     */
    IWmiProvider getWmiProvider();

    /**
     * Obtain the IDirectory for the machine.
     *
     * @since 1.0
     */
    IDirectory getDirectory();

    /**
     * Obtain the session's runspace pool.
     *
     * @since 1.0
     */
    IRunspacePool getRunspacePool();

    /**
     * See <a href="https://docs.microsoft.com/en-us/windows/win32/api/dsrole/ne-dsrole-dsrole_machine_role">DSROLE_MACHINE_ROLE</a>
     *
     * @since 1.5.0
     */
    enum DomainRole {
	STANDALONE_WORKSTATION(0),
	MEMBER_WORKSTATION(1),
	STANDALONE_SERVER(2),
	MEMBER_SERVER(3),
	BACKUP_DC(4),
	PRIMARY_DC(5);

	private int id;

	private DomainRole(int id) {
	    this.id = id;
	}

	public static DomainRole fromID(int id) throws IllegalArgumentException {
	    for (DomainRole role : values()) {
		if (role.id == id) {
		    return role;
		}
	    }
	    throw new IllegalArgumentException(Integer.toString(id));
	}
    }

    /**
     * Get the machine's DomainRole.
     *
     * @since 1.5.0
     */
    DomainRole getDomainRole() throws WmiException;
}
