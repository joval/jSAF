// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.ssh.system;

import jsaf.intf.system.IProcess;

/**
 * An interface defining a process being run over an SSH connection.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.1
 */
public interface ISshProcess extends IProcess {
    /**
     * An enumeration of the different types of SSH processes.
     */
    public enum Type {
	/**
	 * A process that is being executed from an SSH EXEC channel.
	 */
	EXEC,

	/**
	 * A process that is being executed from a pseudo-terminal.
	 */
	EXEC_PTY,

	/**
	 * A process that is being executed from a generic shell prompt.
	 */
	SHELL,

	/**
	 * A process that is being executed from a POSIX shell prompt.
	 */
	POSIX;
    }
}
