// Copyright (C) 2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import jsaf.util.Strings;

/**
 * A utility that iterates through lines of a stream.
*
 * When used with a (text) file, the file is deleted when the end of the iterator has been reached. The effect makes for a
 * file-backed iterator that takes up little memory at runtime.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.9
 */
public class LineIterator implements Iterator<String> {
    private File tempFile = null;
    private BufferedReader reader;
    private String next = null;

    /**
     * Create a LineIterator for a text file. When the end of the iterator has been reached, the file is deleted.
     * This constructor will detect whether the file is GZipped. Detects encoding by reading the file BOM.
     */
    public LineIterator(File tempFile) throws IOException {
	this(tempFile, null);
    }

    /**
     * Create a LineIterator for the file using the specified encoding. Auto-detects GZip-compressed files.
     */
    public LineIterator(File tempFile, Charset encoding) throws IOException {
	this(new FileInputStream(tempFile), encoding);
	this.tempFile = tempFile;
    }

    /**
     * Create a LineIterator for an InputStream; detect encoding.
     */
    public LineIterator(InputStream in) throws IOException {
	this(in, null);
    }

    /**
     * Create a LineIterator for an InputStream using the specified encoding.
     *
     * @param encoding The stream's character encoding. If null, encoding is detected by trying to read a BOM from the stream.
     *                 If there is no BOM, the class defaults to UTF-8.
     */
    public LineIterator(InputStream in, Charset encoding) throws IOException {
	BufferedInputStream bis = new BufferedInputStream(in);
	if (isGzipped(bis)) {
	    bis = new BufferedInputStream(new GZIPInputStream(bis));
	}
	if (encoding == null) {
	    encoding = Streams.detectEncoding(bis);
	}
	reader = new BufferedReader(new InputStreamReader(bis, encoding == null ? Strings.UTF8 : encoding));
    }

    @Override
    protected void finalize() {
	close();
    }

    // Implement Iterator<String>

    public boolean hasNext() {
	if (next == null) {
	    try {
		next = next();
		return true;
	    } catch (NoSuchElementException e) {
		close();
		return false;
	    }
	} else {
	    return true;
	}
    }

    public String next() throws NoSuchElementException {
	if (next == null) {
	    try {
		if (reader == null) {
		    throw new NoSuchElementException(); // previously closed
		} else if ((next = reader.readLine()) == null) {
		    close();
		    throw new NoSuchElementException();
		}
	    } catch (IOException e) {
		throw new NoSuchElementException(e.getMessage());
	    }
	}
	String temp = next;
	next = null;
	return temp;
    }

    public void remove() {
	throw new UnsupportedOperationException();
    }

    // Private

    private static final byte[] GZIP_MAGIC = new byte[] {(byte)0x1f, (byte)0x8b};

    /**
     * Checks the magic bytes to see if the stream is Gzipped, then reset the stream back to the beginning.
     */
    private static boolean isGzipped(BufferedInputStream in) throws IOException {
	in.mark(2);
	byte[] magic = new byte[2];
	for (int i=0; i < magic.length; i++) {
	    int ch = in.read();
	    switch(ch) {
	      case -1: // EOF
		in.reset();
		return false;
	      default:
		magic[i] = (byte)(0xFF & ch);
		break;
	    }
	}
	in.reset();
	return Arrays.equals(GZIP_MAGIC, magic);
    }

    /**
     * Clean up the reader stream and file.
     */
    private void close() {
	if (reader != null) {
	    try {
		reader.close();
	    } catch (IOException e) {
	    }
	    reader = null;
	}
	if (tempFile != null) {
	    if (tempFile.delete()) {
		tempFile = null;
	    }
	}
    }
}
