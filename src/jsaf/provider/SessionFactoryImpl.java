// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;

import jsaf.intf.system.IBaseSession;
import jsaf.intf.system.ISession;
import jsaf.provider.unix.system.UnixSession;
import jsaf.provider.windows.system.WindowsSession;

/**
 * Local provider SessionFactory implementation class.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SessionFactoryImpl extends SessionFactory {
    private File workspace;

    public SessionFactoryImpl (File workspace) {
	this.workspace = workspace;
    }

    // Implement abstract methods of SessionFactory

    public IBaseSession createSession() {
	return createSession(ISession.LOCALHOST);
    }

    public IBaseSession createSession(String target) {
	if (!ISession.LOCALHOST.equals(target)) {
	    throw new IllegalArgumentException(target);
	}
	File wsdir = null;
	if (workspace != null) {
	    wsdir = new File(workspace, target);
	    if (!wsdir.exists()) {
		wsdir.mkdirs();
	    }
	}
	if (System.getProperty("os.name").startsWith("Windows")) {
	    return new WindowsSession(wsdir);
	} else {
	    return new UnixSession(wsdir);
	}
    }
}
