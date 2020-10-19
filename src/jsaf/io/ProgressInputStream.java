// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import jsaf.intf.util.IPublisher;
import jsaf.intf.util.Progress;

public class ProgressInputStream extends InputStream {
    private IPublisher<Progress> publisher;
    private long counter=0, length=0;
    private short lastPct = 0;
    private InputStream in;

    public ProgressInputStream(File f, IPublisher<Progress> publisher) throws IOException {
	init(new FileInputStream(f), f.length(), publisher);
    }

    public ProgressInputStream(InputStream in, long length, IPublisher<Progress> publisher) throws IOException {
	init(in, length, publisher);
    }

    @Override
    public int read() throws IOException {
	try {
	    return in.read();
	} finally {
	    short pct = (short)((counter++ * 100L) / length);
	    if (pct > lastPct) {
		publisher.publish(Progress.UPDATE, new Progress.Update(lastPct = pct, counter));
	    }
	}
    }

    @Override
    public void close() throws IOException {
	in.close();
    }

    // Internal

    static class BufferStream extends ByteArrayOutputStream {
	BufferStream() {
	    super();
	}

	ByteArrayInputStream getInputStream() {
	    return new ByteArrayInputStream(buf, 0, count);
	}

	int length() {
	    return count;
	}
    }

    // Private

    private void init(InputStream in, long length, IPublisher<Progress> publisher) throws IOException {
	this.publisher = publisher;
	if (length > 0) {
	    this.length = length;
	    this.in = in;
	} else {
	    BufferStream buff = new BufferStream();
	    Streams.copy(in, buff);
	    this.length = buff.length();
	    this.in = buff.getInputStream();
	}
    }
}
