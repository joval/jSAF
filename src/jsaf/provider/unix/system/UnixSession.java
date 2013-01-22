// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.system;

import java.io.File;

import jsaf.intf.system.IProcess;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.provider.AbstractSession;
import jsaf.provider.unix.io.UnixFilesystem;

/**
 * A simple session implementation for Unix machines.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class UnixSession extends BaseUnixSession {
    public UnixSession(File wsdir) {
	super();
	this.wsdir = wsdir;
    }

    // Implement ISession

    public boolean connect() {
	if (env == null) {
	    env = new Environment(this);
	}
	if (fs == null) {
	    fs = new UnixFilesystem(this);
	}
	flavor = Flavor.flavorOf(this);
	connected = true;
	return true;
    }

    public void disconnect() {
	connected = false;
    }
}
