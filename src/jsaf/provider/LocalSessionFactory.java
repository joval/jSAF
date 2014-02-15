// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;

import jsaf.intf.io.IFilesystem;
import jsaf.intf.remote.IConnectionSpecification;
import jsaf.intf.remote.IConnectionSpecificationFactory;
import jsaf.intf.ssh.ISshTools;
import jsaf.intf.system.ISession;
import jsaf.provider.unix.system.UnixSession;
import jsaf.provider.windows.system.WindowsSession;

/**
 * Local provider SessionFactory implementation class.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class LocalSessionFactory extends SessionFactory {
    private File workspace;

    public LocalSessionFactory (File workspace) {
	this.workspace = workspace;
    }

    // Implement abstract methods of SessionFactory

    public ISession createSession() {
	return createSession(IConnectionSpecification.LOCALHOST);
    }

    public ISession createSession(IConnectionSpecification target) {
	if (IConnectionSpecification.LOCALHOST != target) {
	    throw new IllegalArgumentException(target.getHostname());
	}
	File wsdir = null;
	if (workspace != null) {
	    wsdir = new File(workspace, ISession.LOCALHOST);
	    if (wsdir.exists()) {
		for (File f : wsdir.listFiles()) {
		    String fname = f.getName();
		    if (fname.endsWith(".tmp")) {
			f.delete();
		    } else if (fname.endsWith(".log")) {
			f.delete();
		    }
		}
	    } else {
		wsdir.mkdirs();
	    }
	}
	ISession session = null;
	switch(target.getType()) {
	  case WINDOWS:
	    session = new WindowsSession(wsdir);
	    break;
	  default:
	    session = new UnixSession(wsdir);
	    break;
	}
	if (workspace == null) {
	    session.getProperties().setProperty(IFilesystem.PROP_CACHE_JDBM, null);
	}
	return session;
    }
}
