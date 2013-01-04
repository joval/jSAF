// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.system;

import jsaf.Message;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.system.IEnvironment;
import jsaf.util.AbstractEnvironment;
import jsaf.util.SafeCLI;

/**
 * A representation of an environment on a Unix machine.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Environment extends AbstractEnvironment {
    public Environment(IUnixSession session) {
	super();
	try {
	    for (String line : SafeCLI.multiLine("env", session, IUnixSession.Timeout.S)) {
		int ptr = line.indexOf("=");
		if (ptr > 0) {
		    props.setProperty(line.substring(0, ptr), line.substring(ptr+1));
		}
	    }
	} catch (Exception e) {
	    session.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	}
    }
}
