// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.apple.system;

import java.io.InputStream;

import jsaf.intf.system.ISession;

/**
 * A representation of an iOS (offline/plist) session.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IiOSSession extends ISession {
    /**
     * Retrieves plist data for the device.
     */
    InputStream getConfigurationProfile();
}
