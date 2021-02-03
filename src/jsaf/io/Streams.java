// Copyright (c) 2011-2020 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.slf4j.cal10n.LocLogger;

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
     * Magic numbers for various compression formats.
     *
     * @since 1.5.0
     */
    public enum Magic {
	/**
	 * Magic bytes for a ZIP file.
	 */
	ZIP(new byte[] {(byte)0x50, (byte)0x4b, (byte)0x03, (byte)0x04}),

	/**
	 * Magic bytes for a BZ2 file.
	 */
	BZ2(new byte[] {'B', 'Z', 'h'}),

	/**
	 * Magic bytes for a GZIP file.
	 */
	GZIP(new byte[] {(byte)0x1f, (byte)0x8b});

	public byte[] bytes() {
	    return bytes;
	}

	// Private

	private byte[] bytes;

	Magic(byte[] bytes) {
	    this.bytes = bytes;
	}
    }

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
     * Measure the length of the InputStream.
     *
     * @since 1.6.6
     */
    public static long measure(InputStream in) throws IOException {
	SizeStream out = new SizeStream();
	copy(in, out);
	return out.size();
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

    /**
     * Convert a string representing a URL or a valid file path to a URL.
     *
     * @since 1.6.6
     */
    public static URL toURL(String str) throws MalformedURLException {
	MalformedURLException ex = null;
	try {
	    return new URL(str);
	} catch (MalformedURLException e) {
	    ex = e;
	}

	File f = new File(str);
	if (f.isFile()) {
	    return f.toURI().toURL();
	}
	MalformedURLException exception = new MalformedURLException(Message.getMessage(Message.ERROR_URL, str));
	exception.initCause(ex);
	throw exception;
    }

    /**
     * Detects and handles compression of the URL content.
     *
     * @return an instance of Zipped if the URL points to a ZIP file, otherwise returns the appropriate decompression implementation.
     *
     * @since 1.5.0
     */
    public static final InputStream open(URL url) throws IOException {
	BufferedInputStream in = new BufferedInputStream(url.openStream());
	byte[] buff = new byte[4];
	in.mark(4);
	Streams.readFully(in, buff);
	in.reset();
	if (Arrays.equals(Magic.ZIP.bytes(), buff)) {
	    Zipped zip = new Zipped(new ZipInputStream(in));
	    zip.getNextEntry();
	    return zip;
	} else if (Arrays.equals(Magic.GZIP.bytes(), Arrays.copyOfRange(buff, 0, Magic.GZIP.bytes().length))) {
	    return new GZIPInputStream(in);
	} else if (Arrays.equals(Magic.BZ2.bytes(), Arrays.copyOfRange(buff, 0, Magic.BZ2.bytes().length))) {
	    return new CBZip2InputStream(in);
	} else {
	    return in;
	}
    }

    /**
     * Detects and handles compression of the URL content.
     *
     * @return a CachedURLConnection, backed by a file cache.
     *
     * @since 1.6.3
     */
    public static final CachedURLConnection openConnection(URL url) throws IOException {
	return new CachedURLConnection(url);
    }

    /**
     * Method for safely closing whatever is returned by the open(URL) method (no null check needed).
     *
     * @since 1.5.0
     */
    public static final void close(InputStream in) {
	if (in == null) return;
	try {
	    if (in instanceof Unclosable) {
		((Unclosable)in).reallyClose();
	    } else {
		in.close();
	    }
	} catch (IOException e) {
	}
    }

    /**
    /**
     * Method for safely closing an OutputStream (no null check needed).
     *
     * @since 1.6.6
     */
    public static final void close(OutputStream out) {
	if (out == null) return;
	try {
	    out.close();
	} catch (IOException e) {
	}
    }

    /**
     * Wraps an InputStream to prevent it from being closed.
     *
     * @since 1.6.4
     */
    public static InputStream unclosable(InputStream in) {
	return new Unclosable(in);
    }

    /**
     * Wraps a ZipInputStream, primarily for the purpose of making it more difficult to close (it can only be
     * closed using the ScapStream.close method).
     *
     * @since 1.5.0
     */
    public static class Zipped extends Unclosable {
	private ZipEntry currentEntry;

	public Zipped(ZipInputStream in) {
	    super(in);
	}

	public Zipped(ZipInputStream in, ZipEntry entry) {
	    this(in);
	    currentEntry = entry;
	}

	public ZipEntry getNextEntry() throws IOException {
	    return currentEntry = ((ZipInputStream)in).getNextEntry();
	}

	public ZipEntry getCurrentEntry() {
	    return currentEntry;
	}
    }

    /**
     * Used to measure the length of another InputStream.
     *
     * @since 1.6.6
     */
    public static class SizeStream extends OutputStream {
	private long counter;

	public SizeStream() {
	    counter = 0L;
	}

	public long size() {
	    return counter;
	}

	public void write(int b) {
	    counter++;
	}
    }

    // Private

    private static class Unclosable extends FilterInputStream {
	Unclosable(InputStream in) {
	    super(in);
	}

	@Override
	public void close() throws IOException {
	    // no-op
	}

	// Internal

	void reallyClose() {
	    try {
		super.close();
	    } catch (IOException e) {
	    }
	}
    }

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
