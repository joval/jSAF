// Copyright (C) 2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
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
     * This constructor will auto-detect whether the file is GZipped.
     */
    public LineIterator(File tempFile) throws IOException {
	this(tempFile, isGzipped(tempFile));
    }

    /**
     * Create a LineIterator for the file (which is gzipped, if indicated).
     */
    public LineIterator(File tempFile, boolean gzipped) throws IOException {
	this.tempFile = tempFile;
	InputStream in = new FileInputStream(tempFile);
	if (gzipped) {
	    try {
		in = new GZIPInputStream(in);
	    } catch (IOException e) {
		close();
		throw e;
	    }
	}
	reader = new BufferedReader(new InputStreamReader(in, Strings.UTF8));
    }

    /**
     * Create a LineIterator for an InputStream.
     */
    public LineIterator(InputStream in) {
	reader = new BufferedReader(new InputStreamReader(in, Strings.UTF8));
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

    private static boolean isGzipped(File f) throws IOException {
	if (f.length() < 2) {
	    return false;
	}
	RandomAccessFile raf = null;
	try {
	    raf = new RandomAccessFile(f, "r");
	    byte[] magic = new byte[2];
	    raf.readFully(magic);
	    return Arrays.equals(GZIP_MAGIC, magic);
	} finally {
	    if (raf != null) {
		try {
		    raf.close();
		} catch (IOException e) {
		}
	    }
	}
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