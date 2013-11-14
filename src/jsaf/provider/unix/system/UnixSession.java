// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.system;

import java.io.File;

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

    @Override
    public boolean connect() {
	connected = true; // set this now so that createProcess will work
	if (env == null) {
	    env = new Environment(this);
	}
	if (fs == null) {
	    fs = new UnixFilesystem(this);
	}
	flavor = Flavor.flavorOf(this);
	return true;
    }
}
