// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import jsaf.intf.util.IPublisher;
import jsaf.intf.util.ISubscriber;

/**
 * An implementation of IPublisher.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Publisher<T extends Enum> implements IPublisher<T> {
    private Collection<ISubscriber<T>> subscribers;

    public Publisher() {
	subscribers = new HashSet<ISubscriber<T>>();
    }

    public void subscribe(ISubscriber<T> subscriber) {
	synchronized(subscribers) {
	    subscribers.add(subscriber);
	}
    }

    public void unsubscribe(ISubscriber<T> subscriber) {
	synchronized(subscribers) {
	    subscribers.remove(subscriber);
	}
    }

    public void publish(T msg, Object arg) {
	new Thread(new PubTask(msg, arg), "jSAF Event Publisher").start();
    }

    // Internal

    class PubTask implements Runnable {
	private T msg;
	private Object arg;
	private Collection<ISubscriber<T>> subscribers;

	PubTask(T msg, Object arg) {
	    this.msg = msg;
	    this.arg = arg;
	    synchronized(Publisher.this.subscribers) {
		subscribers = new ArrayList<ISubscriber<T>>(Publisher.this.subscribers);
	    }
	}

	// Implement Runnable

	public void run() {
	    for (ISubscriber<T> subscriber : subscribers) {
		subscriber.notify(Publisher.this, msg, arg);
	    }
	}
    }
}
