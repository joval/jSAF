// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.IOException;
import java.io.OutputStream;

import jsaf.intf.util.IPublisher;
import jsaf.intf.util.Progress;

/**
 * OutputStream that publishes Progress.UPDATE events as the underlying stream is written.
 *
 * @since 1.6.6
 * @author David A. Solin
 * @version %I% %G%
 */
public class ProgressOutputStream extends OutputStream {
    private IPublisher<Progress> publisher;
    private int length, bytesWritten=0;
    private short lastPct = 0;
    private OutputStream out;

    public ProgressOutputStream(OutputStream out, int length, IPublisher<Progress> publisher) throws IOException {
	if (publisher == null) {
	    throw new NullPointerException();
	}
	this.publisher = publisher;
	this.out = out;
	this.length = length;
    }

    @Override
    public void write(int b) throws IOException {
	out.write(b);
	update(1);
    }

    @Override
    public void write(byte[] buff) throws IOException {
	write(buff, 0, buff.length);
    }

    @Override
    public void write(byte[] buff, int offset, int length) throws IOException {
	out.write(buff, offset, length);
	update(length);
    }

    @Override
    public void close() throws IOException {
	out.close();
    }

    // Private


    private void update(int len) {
	if (len > 0) {
	    bytesWritten += len;
	    short pct = (short)((bytesWritten * 100) / length);
	    if (pct > lastPct) {
		publisher.publish(Progress.UPDATE, new Progress.Update(lastPct = pct, bytesWritten));
	    }
	}
    }
}
