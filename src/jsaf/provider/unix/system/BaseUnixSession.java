// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.system;

import jsaf.intf.unix.system.IUnixSession;
import jsaf.provider.AbstractSession;

/**
 * A simple session implementation for Unix machines.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public abstract class BaseUnixSession extends AbstractSession implements IUnixSession {
    protected Flavor flavor = Flavor.UNKNOWN;

    protected BaseUnixSession() {
	super();
    }

    @Override
    protected String getOverrideKey(String key) {
	if (flavor == null) {
	    // during initialization of the super-class
	    return null;
	} else {
	    switch(flavor) {
	      case UNKNOWN:
		return null;

	      default:
		return new StringBuffer(flavor.value()).append(".").append(key).toString();
	    }
	}
    }

    // Implement IUnixSession

    public Flavor getFlavor() {
	return flavor;
    }

    // Implement IBaseSession

    public Type getType() {
	return Type.UNIX;
    }
}
