// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.system;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.system.IEnvironment;
import jsaf.intf.windows.identity.IDirectory;
import jsaf.intf.windows.io.IWindowsFilesystem;
import jsaf.intf.windows.powershell.IRunspacePool;
import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.registry.IStringValue;
import jsaf.intf.windows.registry.IValue;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.io.fs.AbstractFilesystem;
import jsaf.provider.ComputerSystemSession;
import jsaf.provider.windows.identity.Directory;
import jsaf.provider.windows.io.WindowsFilesystem;
import jsaf.provider.windows.powershell.RunspacePool;
import jsaf.provider.windows.registry.Registry;
import jsaf.provider.windows.wmi.WmiProvider;

/**
 * Windows implementation of ISession for local machines, using JACOB for WMI access via COM.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class WindowsSession extends ComputerSystemSession implements IWindowsSession {
    static {
	//
	// Load the JACOB DLL
	//
	com.jacob.com.LibraryLoader.loadJacobLibrary();
    }

    private WmiProvider wmi;
    private boolean is64bit = false;
    private Registry reg32, reg;
    private IWindowsFilesystem fs32;
    private Directory directory = null;
    private RunspacePool runspaces = null;
    private List<String> baseCommand = Arrays.asList("cmd", "/c");
    private View accessorView = null;

    public WindowsSession(File wsdir) {
	super();
	this.wsdir = wsdir;
    }

    protected List<String> getBaseCommand() {
	return baseCommand;
    }

    // Implement ILoggable

    @Override
    public void setLogger(LocLogger logger) {
	super.setLogger(logger);
	if (fs32 != null && !fs32.equals(fs)) {
	    fs32.setLogger(logger);
	}
	if (wmi != null) {
	    wmi.setLogger(logger);
	}
	if (directory != null) {
	    directory.setLogger(logger);
	}
    }

    // Implement ISession

    @Override
    public String getUsername() {
	return System.getenv("USERDOMAIN") + "\\" + System.getProperty("user.name");
    }

    @Override
    public boolean connect() {
	connected = true; // set this now so createProcess will work
	if (env == null) {
	    try {
		env = new Environment(this);
	    } catch (Exception e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		return connected = false;
	    }
	}
	is64bit = ((Environment)env).is64bit();
	if (is64bit) {
	    if ("64".equals(System.getProperty("sun.arch.data.model"))) {
		accessorView = View._64BIT;
	    } else {
		accessorView = View._32BIT;
		StringBuffer cmd = new StringBuffer(System.getenv("SystemRoot")).append("\\SysNative\\cmd.exe");
		baseCommand = Arrays.asList(cmd.toString(), "/c");
	    }
	    logger.trace(Message.STATUS_WINDOWS_BITNESS, "64");
	} else {
	    accessorView = View._32BIT;
	    logger.trace(Message.STATUS_WINDOWS_BITNESS, "32");
	}
	return connected = true;
    }

    @Override
    public void disconnect() {
	if (runspaces != null) {
	    runspaces.shutdown();
	}
	if (wmi != null) {
	    wmi.deregister();
	}
	env = null;
	connected = false;
    }

    @Override
    public void dispose() {
	super.dispose();
	if (fs32 instanceof AbstractFilesystem) {
	    ((AbstractFilesystem)fs32).dispose();
	}
    }

    public Type getType() {
	return Type.WINDOWS;
    }

    // Implement IComputerSystem

    @Override
    public String getMachineName() {
	if (isConnected()) {
	    try {
		return reg.getStringValue(IRegistry.Hive.HKLM, IRegistry.COMPUTERNAME_KEY, IRegistry.COMPUTERNAME_VAL);
	    } catch (Exception e) {
		logger.warn(Message.ERROR_MACHINENAME);
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}
	return getHostname();
    }

    // Implement IWindowsSession

    public IRunspacePool getRunspacePool() {
	if (runspaces == null) {
	    runspaces = new RunspacePool(this, 100);
	}
	return runspaces;
    }

    public IDirectory getDirectory() {
	if (directory == null) {
	    try {
	        directory = new Directory(this);
	    } catch (Exception e) {
	        logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}
	return directory;
    }

    public View getNativeView() {
	return is64bit ? View._64BIT : View._32BIT;
    }

    public boolean supports(View view) {
	switch(view) {
	  case _32BIT:
	    return true;
	  case _64BIT:
	  default:
	    return is64bit;
	}
    }

    public IRegistry getRegistry(View view) {
	switch(view) {
	  case _32BIT:
	    if (reg32 != null) {
		return reg32;
	    } else if (is64bit) {
		try {
		    return reg32 = new Registry(this, View._32BIT);
		} catch (Exception e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		}
	    }
	    // fall-thru

	  default:
	    if (reg == null) {
		try {
		    reg = new Registry(this);
		    if (!is64bit) {
			reg32 = reg;
		    }
		} catch (Exception e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		}
	    }
	    return reg;
	}
    }

    @Override
    public IFilesystem getFilesystem() {
	return getFilesystem(getNativeView());
    }

    public IWindowsFilesystem getFilesystem(View view) {
	switch(view) {
	  case _32BIT:
	    if (fs32 != null) {
		return fs32;
	    } else if (is64bit) {
		return fs32 = new WindowsFilesystem(this, View._32BIT, accessorView);
	    }
	    // fall-thru

	  default:
	    if (fs == null) {
		if (is64bit) {
		    fs = new WindowsFilesystem(this, View._64BIT, accessorView);
		} else {
		    fs32 = new WindowsFilesystem(this, View._32BIT, accessorView);
		    fs = fs32;
		}
	    }
	    return (IWindowsFilesystem)fs;
	}
    }

    public IWmiProvider getWmiProvider() {
	if (wmi == null) {
	    wmi = new WmiProvider(this, getProperties().getLongProperty(IWmiProvider.PROP_WMI_TIMEOUT));
	    wmi.register();
	}
	return wmi;
    }
}
