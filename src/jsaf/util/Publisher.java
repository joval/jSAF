// Copyright (C) 2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
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
    private String publisherThreadName;
    private boolean stopping = false;

    private static final String DEFAULT_NAME = "Event Publisher Thread";

    protected LocLogger logger;

    public Publisher() {
	this(DEFAULT_NAME);
    }

    public Publisher(String publisherThreadName) {
	this.publisherThreadName = publisherThreadName;
	subscribers = new HashSet<ISubscriber<T>>();
	queue = new LinkedBlockingQueue<QueueEntry>();
	logger = Message.getLogger();
	thread = new Thread(this, publisherThreadName);
	thread.setDaemon(true);
	thread.start();
    }

    // Implement IPublisher<T>

    public void subscribe(ISubscriber<T> subscriber) {
	synchronized(subscribers) {
	    subscribers.add(subscriber);
	}
    }

    public void unsubscribe(ISubscriber<T> subscriber) {
	unsubscribe(subscriber, 0L);
    }

    public void unsubscribe(ISubscriber<T> subscriber, long maxWait) {
	try {
	    if (maxWait > 0) {
		long finishBy = System.currentTimeMillis() + maxWait;
		for (int i=0; System.currentTimeMillis() < finishBy && queue.size() > 0; i++) {
		    Thread.sleep(10);
		}
	    }
	} catch (InterruptedException e) {
	} finally {
	    synchronized(subscribers) {
		subscribers.remove(subscriber);
	    }
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
	    if (!stopping) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}
    }

    // Implement IDisposable

    public void dispose() {
	if (thread != null) {
	    Iterator<ISubscriber<T>> iter = subscribers.iterator();
	    while(iter.hasNext()) {
		unsubscribe(iter.next());
	    }
	    logger.debug(Message.STATUS_PUBLISHER_STOP, publisherThreadName);
	    stopping = true;
	    thread.interrupt();
	    thread = null;
	}
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
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
