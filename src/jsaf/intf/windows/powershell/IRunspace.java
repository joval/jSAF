// Copyright (C) 2012-2020 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.powershell;

import java.io.InputStream;
import java.net.URL;

import jsaf.intf.util.ILoggable;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.provider.windows.powershell.PowershellException;

/**
 * An interface to a Powershell runspace.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IRunspace extends ILoggable {
    /**
     * Get the identifier for this runspace, which is guaranteed to be unique for the containing IRunspacePool. This
     * identifier can be used to track whether or not a runspace returned by the pool has already had an assembly
     * or module loaded into it.
     *
     * @since 1.0
     */
    String getId();

    /**
     * Get the view for this runspace.
     *
     * @since 1.0
     */
    IWindowsSession.View getView();

    /**
     * Load a Powershell module into the runspace from an InputStream.
     *
     * @since 1.0
     */
    void loadModule(InputStream in) throws PowershellException;

    /**
     * Load a Powershell module into the runspace from an InputStream, using the specified timeout (in millis).
     *
     * @since 1.0
     */
    void loadModule(InputStream in, long timeout) throws PowershellException;

    /**
     * Load a Powershell module into the runspace from a URL.
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.5
     */
    void loadModule(URL url) throws PowershellException;

    /**
     * Load a Powershell module into the runspace from a URL, using the specified timeout (in millis).
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.5
     */
    void loadModule(URL url, long timeout) throws PowershellException;

    /**
     * Load an assembly (DLL) into the runspace from an InputStream.
     *
     * @since 1.1
     */
    void loadAssembly(InputStream in) throws PowershellException;

    /**
     * Load an assembly (DLL) into the runspace from an InputStream, using the specified timeout (in millis).
     *
     * @since 1.1
     */
    void loadAssembly(InputStream in, long timeout) throws PowershellException;

    /**
     * Load an assembly (DLL) into the runspace from a URL.
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.5
     */
    void loadAssembly(URL url) throws PowershellException;

    /**
     * Load an assembly (DLL) into the runspace from a URL, using the specified timeout (in millis).
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.5
     */
    void loadAssembly(URL url, long timeout) throws PowershellException;

    /**
     * Load a module based on an assembly from an InputStream.
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.3
     */
    void loadModuleAssembly(InputStream in) throws PowershellException;

    /**
     * Load a module based on an assembly from an InputStream, using the specified timeout (in millis).
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.3
     */
    void loadModuleAssembly(InputStream in, long timeout) throws PowershellException;

    /**
     * Load a module based on an assembly from a URL.
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.5
     */
    void loadModuleAssembly(URL url) throws PowershellException;

    /**
     * Load a module based on an assembly from a URL, using the specified timeout (in millis).
     *
     * @throws PowershellException if there is a Powershell syntactical error with the module contents
     *
     * @since 1.3.5
     */
    void loadModuleAssembly(URL url, long timeout) throws PowershellException;

    /**
     * Invoke a command or module.
     *
     * @return Text output from the command
     *
     * @throws PowershellException if the command causes Powershell to raise an exception
     *
     * @since 1.0
     */
    String invoke(String command) throws PowershellException;

    /**
     * Invoke a command or module with the specified read timeout (in millis).
     *
     * @since 1.0
     */
    String invoke(String command, long timeout) throws PowershellException;

    /**
     * Invoke a pipeline with the default read timeout.
     *
     * @since 1.5.1
     */
    String invoke(IPipeline pipeline) throws PowershellException;

    /**
     * Invoke a pipeline with the specified read timeout (in millis).
     *
     * @since 1.5.1
     */
    String invoke(IPipeline pipeline, long timeout) throws PowershellException;
}
