// Copyright (C) 2014, jOVAL.org.  All rights reserved.

package jsaf.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Breaks a single Iterator&lt;String&gt; into multiple iterators, using the specified "boundary" string to determine where
 * to perform the breaks.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class BoundaryIterator implements Iterator<String> {
    private Iterator<String> iter;
    private String boundary=null, next=null;
    private boolean stop;

    public BoundaryIterator(Iterator<String> iter, String boundary) {
        this.iter = iter;
        this.boundary = boundary;
        stop = false;
    }

    /**
     * Step across a boundary.
     *
     * @return true if something is there, false if the last boundary was the absolute terminus.
     */
    public boolean step() {
        if (iter.hasNext()) {
            stop = false;
            return true;
        } else {
            return false;
        }
    }

    // Implement Iterator<String>

    public boolean hasNext() {
        if (stop) {
            return false;
        } else if (next == null) {
            try {
                next = next();
                return true;
            } catch (NoSuchElementException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    public String next() {
        if (stop) {
            throw new NoSuchElementException();
        } else if (next == null) {
            if (iter.hasNext()) {
                String temp = iter.next();
                if (boundary.equals(temp)) {
                    stop = true;
                    throw new NoSuchElementException();
                } else {
                    next = temp;
                    return next();
                }
            } else {
                throw new NoSuchElementException();
            }
        } else {
            String temp = next;
            next = null;
            return temp;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
