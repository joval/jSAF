// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import jsaf.intf.util.IPublisher;
import jsaf.intf.util.ISubscriber;

/**
 * An implementation of IPublisher.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Publisher<T extends Enum> implements IPublisher<T>, Runnable {
    private Collection<ISubscriber<T>> subscribers;
    private LinkedBlockingQueue<QueueEntry> queue;
    private Thread thread;

    public Publisher() {
	subscribers = new HashSet<ISubscriber<T>>();
	queue = new LinkedBlockingQueue<QueueEntry>();
    }

    public void start() {
	if (thread == null) {
	    thread = new Thread(this, "Event Publisher Thread");
	    thread.setDaemon(true);
	    thread.start();
	} else {
	    throw new IllegalStateException();
	}
    }

    public void stop() {
	if (thread == null) {
	    throw new IllegalStateException();
	} else {
	    thread.interrupt();
	    thread = null;
	}
    }

    // Implement IPublisher<T>

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
	try {
	    queue.put(new QueueEntry(msg, arg));
	} catch (InterruptedException e) {
	}
    }

    // Implement Runnable

    public void run() {
	try {
	    while(true) {
		queue.take().publish();
	    }
	} catch (InterruptedException e) {
	}
    }

    // Internal

    class QueueEntry {
	private T msg;
	private Object arg;

	QueueEntry(T msg, Object arg) {
	    this.msg = msg;
	    this.arg = arg;
	}

	void publish() {
	    Collection<ISubscriber<T>> subscribers;
	    synchronized(Publisher.this.subscribers) {
		subscribers = new ArrayList<ISubscriber<T>>(Publisher.this.subscribers);
	    }
	    for (ISubscriber<T> subscriber : subscribers) {
		subscriber.notify(Publisher.this, msg, arg);
	    }
	}
    }
}
