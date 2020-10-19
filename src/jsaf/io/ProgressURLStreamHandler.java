// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import jsaf.intf.util.IPublisher;
import jsaf.intf.util.ISubscriber;
import jsaf.intf.util.Progress;
import jsaf.util.Publisher;

/**
 * A URLStreamHandler that can be monitored for progress.
 *
 * @since 1.6.3
 * @author David A. Solin
 * @version %I% %G%
 */
public class ProgressURLStreamHandler extends URLStreamHandler implements IPublisher<Progress> {
    private Publisher<Progress> publisher;

    public ProgressURLStreamHandler() {
        publisher = new Publisher<Progress>();
	publisher.start();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        if (url.getProtocol().equals("progress")) {
            return new ProgressURLConnection(url);
        } else {
            throw new MalformedURLException("Unknown protocol: " + url.getProtocol());
        }
    }

    // Implement IPublisher<Progress>

    public void publish(Progress msg, Object arg) {
        publisher.publish(msg, arg);
    }

    public void subscribe(ISubscriber<Progress> subscriber) {
        publisher.subscribe(subscriber);
    }

    public void unsubscribe(ISubscriber<Progress> subscriber) {
        publisher.unsubscribe(subscriber);
    }

    // Internal

    class ProgressURLConnection extends URLConnection {
        ProgressURLConnection(URL url) {
            super(url);
        }

        // URLConnection overrides

        public void connect() {
            connected = true;
        }

        public InputStream getInputStream() throws IOException {
            if (!connected) {
                connect();
            }
            return new ProgressInputStream(new File(url.toString().substring(9)), ProgressURLStreamHandler.this.publisher);
        }
    }
}
