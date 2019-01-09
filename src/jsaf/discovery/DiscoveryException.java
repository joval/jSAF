// Copyright (c) 2018 JovalCM.com.  All rights reserved.

package jsaf.discovery;

/**
 * An exception class for discovery operations.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.4
 */
public class DiscoveryException extends Exception {
    public DiscoveryException(String msg) {
	super(msg);
    }

    public DiscoveryException(Throwable cause) {
	super(cause);
    }
}
