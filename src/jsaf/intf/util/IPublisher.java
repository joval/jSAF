// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * An interface describing an event producer.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface IPublisher <T extends Enum> {
    /**
     * Add a subscriber for events published by an IPublisher instance.
     */
    public void subscribe(ISubscriber<T> subscriber);

    /**
     * Remove a subscriber. No-op if the subscriber is not already subscribed to notifications from this publisher.
     *
     * @param subscriber the subscriber to be removed
     */
    public void unsubscribe(ISubscriber<T> subscriber);

    /**
     * Publish an event to subscribers. All subscribers will receive a corresponding notify call.
     *
     * @see ISubscriber#notify(IPublisher, Enum, Object)
     * @param msg delineates the event type
     * @param arg an event-type-specific argument carrying event data
     */
    public void publish(T msg, Object arg);
}
