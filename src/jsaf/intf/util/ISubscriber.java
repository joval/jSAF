// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * An interface describing a subscriber to events.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ISubscriber <T extends Enum> {
    /**
     * Receive a notification about an event.
     */
    public void notify(IPublisher<T> publisher, T msg, Object arg);
}
