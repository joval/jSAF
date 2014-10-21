// Copyright (C) 2014, jOVAL.org.  All rights reserved.

package jsaf.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsaf.intf.io.IReader;

/**
 * An implementation of a SafeCLI.IReader that can be used to break down lines of output into discrete groups, demarcated by
 * the specified boundary line.  An optional line continuation prefix can be used to signify a logical line of output that
 * consists of multiple physical lines.  Blank lines are ignored.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class BoundaryReaderHandler implements SafeCLI.IReaderHandler {
    private List<List<String>> list;
    private String boundary, lineContinuationPrefix;

    public BoundaryReaderHandler(String boundary) {
	this(boundary, null);
    }

    public BoundaryReaderHandler(String boundary, String lineContinuationPrefix) {
	this.boundary = boundary;
	this.lineContinuationPrefix = lineContinuationPrefix;
    }

    public Iterator<List<String>> iterator() {
        return list.iterator();
    }

    // Implement IReaderHandler

    public void handle(IReader reader) throws IOException {
        list = new ArrayList<List<String>>();
        List<String> current = new ArrayList<String>();
        String line = null;
        while((line = reader.readLine()) != null) {
            if (boundary.equals(line)) {
                list.add(current);
                current = new ArrayList<String>();
            } else if (lineContinuationPrefix != null && line.startsWith(lineContinuationPrefix)) {
                int index = current.size() - 1;
                current.set(index, new StringBuffer(current.get(index)).append("\n").append(line).toString());
            } else if (line.length() > 0) {
                current.add(line);
            }
        }
        list.add(current);
    }
}
