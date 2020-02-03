// Copyright (c) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Vector;

import jsaf.Message;
import jsaf.util.Strings;

/**
 * Some stream utilities.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.2
 */
public class Streams {
    /**
     * Useful in debugging...
     *
     * @since 1.2
     */
    public static final void hexDump(byte[] buff, PrintStream out) {
	int numRows = buff.length / 16;
	if (buff.length % 16 > 0) numRows++; // partial row

	int ptr = 0;
	for (int i=0; i < numRows; i++) {
	    for (int j=0; j < 16; j++) {
		if (ptr < buff.length) {
		    if (j > 0) System.out.print(" ");
		    String iHex = Integer.toHexString((int)buff[ptr++]);
		    if (iHex.length() == 1) {
			out.print("0");
		    }
		    out.print(iHex);
		} else {
		    break;
		}
	    }
	    out.println("");
	}
    }

    /**
     * Read from the stream until the buffer is full.
     *
     * @since 1.2
     */
    public static final void readFully(InputStream in, byte[] buff) throws IOException {
	if (buff.length > 0) {
	    int offset = 0;
	    do {
		int bytesRead = in.read(buff, offset, buff.length-offset);
		if (bytesRead == -1) {
		    throw new EOFException(Message.getMessage(Message.ERROR_EOS));
		} else {
		    offset += bytesRead;
		}
	    } while (offset < buff.length);
	}
    }

    /**
     * Shortcut for copyAsync(in, out, false).
     *
     * @since 1.2
     */
    public static void copyAsync(InputStream in, OutputStream out) {
	copyAsync(in, out, false);
    }

    /**
     * Copy from in to out asynchronously (i.e., in a new Thread). Closes the InputStream when done, and closes
     * the OutputStream according to closeOut.
     *
     * @since 1.3.5
     */
    public static void copyAsync(InputStream in, OutputStream out, boolean closeOut) {
	new Copier(in, out, closeOut).start();
    }

    /**
     * Copy completely from in to out.  Closes the InputStream when done, but not the OutputStream.
     *
     * @since 1.2
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
	copy(in, out, false);
    }

    /**
     * Copy completely from in to out.  Closes the InputStream when done.  Closes the OutputStream according to closeOut.
     *
     * @since 1.2
     */
    public static void copy(InputStream in, OutputStream out, boolean closeOut) throws IOException {
	Copier copier = new Copier(in, out, closeOut);
	copier.run();
	if (copier.hasError()) {
	    throw copier.error();
	}
    }

    /**
     * Get an OutputStream to nowhere.
     *
     * @since 1.3
     */
    public static OutputStream devNull() {
	return DEVNULL;
    }

    /**
     * Read the contents of a file into a String.
     *
     * @since 1.3.5
     */
    public static String readAsString(File f, Charset charset) throws IOException {
	return readAsString(new FileInputStream(f), charset);
    }

    /**
     * Read the contents of an InputStream into a String.
     *
     * @since 1.3.5
     */
    public static String readAsString(InputStream in, Charset charset) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	copy(in, out);
	return new String(out.toByteArray(), charset);
    }

    /**
     * Read the BOM (Byte-Order marker) from a stream.
     *
     * Starting in 1.4.6, the implementation will return null and reset the stream to the beginning if the
     * stream supports mark/reset, instead of throwing a CharacterCodingException.
     *
     * @since 1.2
     */
    public static Charset detectEncoding(InputStream in) throws IOException {
	boolean buffered = in.markSupported();
	if (buffered) {
	    in.mark(3);
	}
	switch(in.read()) {
	  case 0xEF:
	    if (in.read() == 0xBB && in.read() == 0xBF) {
		return Strings.UTF8;
	    }
	    break;
	  case 0xFE:
	    if (in.read() == 0xFF) {
		return Strings.UTF16;
	    }
	    break;
	  case 0xFF:
	    if (in.read() == 0xFE) {
		return Strings.UTF16LE;
	    }
	    break;
	}
	if (buffered) {
	    in.reset();
	    return null;
	} else {
	    throw new java.nio.charset.CharacterCodingException();
	}
    }

    /**
     * Read a line from an InputStream.
     *
     * @since 1.3.7
     */
    public static String readLine(InputStream in, Charset charset) throws IOException {
	ByteArrayOutputStream buff = new ByteArrayOutputStream();
	int ch = -1;
	while((ch = in.read()) != -1) {
	    switch(ch) {
	      case '\n':
		byte[] data = buff.toByteArray();
		if (data.length == 0) {
		    return "";
		} else if (data[data.length - 1] == '\r') {
		    return new String(buff.toByteArray(), 0, data.length - 1, charset);
		} else {
		    return new String(buff.toByteArray(), charset).trim();
		}
	      default:
		buff.write(ch);
		break;
	    }
	}
	throw new EOFException();
    }

    // Private

    private static class Copier implements Runnable {
	InputStream in;
	OutputStream out;
	IOException error;
	boolean closeOut;
	Thread thread;

	Copier(InputStream in, OutputStream out, boolean closeOut) {
	    this.in = in;
	    this.out = out;
	    this.closeOut = closeOut;
	    error = null;
	}

	boolean hasError() {
	    return error != null;
	}

	IOException error() {
	    return error;
	}

	void start() {
	    (thread = new Thread(this)).start();
	}

	// Implement Runnable

	public void run() {
	    try {
		byte[] buff = new byte[1024];
		int len = 0;
		while ((len = in.read(buff)) > 0) {
		    out.write(buff, 0, len);
		    out.flush();
		}
	    } catch (IOException e) {
		if (thread == null) {
		    error = e;
		} else {
		    Message.getLogger().warn(Message.ERROR_EXCEPTION, e);
		}
	    } finally {
		try {
		    in.close();
		} catch (IOException e) {
		}
		if (closeOut) {
		    try {
			out.close();
		    } catch (IOException e) {
		    }
		}
	    }
	}
    }

    private static final OutputStream DEVNULL = new DevNull();

    private static class DevNull extends OutputStream {
	private DevNull() {
	}

	public void write(int b) {
	}

	public void write(byte[] b) {
	}

	public void write(byte[] b, int offset, int len) {
	}
    }
}
