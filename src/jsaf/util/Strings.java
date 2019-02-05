// Copyright (C) 2011-2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.NoSuchElementException;

/**
 * Apparently there are still a few things that haven't yet been packed into java.lang.String!
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.2
 */
public class Strings {
    /**
     * Line-feed character (as a String).
     *
     * @since 1.3
     */
    public static final String LF = System.getProperty("line.separator");

    /**
     * The line separator on the local machine.
     *
     * @since 1.2
     * @deprecated since 1.3.5. Use LF instead.
     */
    @Deprecated public static final String LOCAL_CR = LF;

    /**
     * An ascending Comparator for Strings.
     *
     * @since 1.2
     */
    public static final Comparator<String> COMPARATOR = new StringComparator(true);

    /**
     * ASCII charset.
     *
     * @since 1.2
     */
    public static final Charset ASCII = Charset.forName("US-ASCII");

    /**
     * UTF8 charset.
     *
     * @since 1.2
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * UTF16 charset.
     *
     * @since 1.2
     */
    public static final Charset UTF16 = Charset.forName("UTF-16");

    /**
     * UTF16 Little Endian charset.
     *
     * @since 1.2
     */
    public static final Charset UTF16LE = Charset.forName("UTF-16LE");

    /**
     * Just like String.join, except for pre-JDK 1.8
     *
     * @since 1.4
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
	return join(delimiter, Arrays.<CharSequence>asList(elements));
    }

    /**
     * Just like String.join, except for pre-JDK 1.8
     *
     * @since 1.4
     */
    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
	StringBuffer sb = new StringBuffer();
	int i = 0;
	for (CharSequence element : elements) {
	    if (i > 0) {
		sb.append(delimiter);
	    }
	    sb.append(element);
	}
	return sb.toString();
    }

    /**
     * Sort the array from A-&gt;Z (ascending ordering).
     *
     * @since 1.2
     */
    public static final String[] sort(String[] array) {
	return sort(array, true);
    }

    /**
     * Arrays can be sorted ascending or descending.
     *
     * @param asc true for ascending (A-&gt;Z), false for descending (Z-&gt;A).
     *
     * @since 1.2
     */
    public static final String[] sort(String[] array, boolean asc) {
	Arrays.sort(array, new StringComparator(asc));
	return array;
    }

    /**
     * A StringTokenizer operates on single-character tokens. This acts on a delimiter that is a multi-character String.
     *
     * @since 1.2
     */
    public static Iterator<String> tokenize(String target, String delimiter) {
	return new StringTokenIterator(target, delimiter);
    }

    /**
     * Gives you an option to keep any zero-length tokens at the ends of the target, if it begins or ends with the delimiter.
     * This guarantees that you get one token for every instance of the delimiter in the target String.
     *
     * @since 1.2
     */
    public static Iterator<String> tokenize(String target, String delimiter, boolean trim) {
	return new StringTokenIterator(target, delimiter, trim);
    }

    /**
     * Like tokenize, but skips instances of the delimiter that are preceded by an escape ('\') character.
     *
     * @since 1.3.7
     */
    public static Iterator<String> tokenizeUnescaped(String target, String delimiter, boolean trim) {
	return new StringTokenIterator(target, delimiter, trim, true);
    }

    /**
     * Convert an Iterator of Strings to a List.
     *
     * @since 1.2
     */
    public static List<String> toList(Iterator<String> iter) {
	List<String> list = new ArrayList<String>();
	while (iter.hasNext()) {
	    list.add(iter.next());
	}
	return list;
    }

    /**
     * Wrap an Iterator in an Iterable.
     *
     * @since 1.3
     */
    public static Iterable<String> iterable(final Iterator<String> iterator) {
	return new Iterable<String>() {
	    public Iterator<String> iterator() {
		return iterator;
	    }
	};
    }

    /**
     * Strip quotes from a quoted String. If the string is not quoted, the original is returned.
     *
     * @since 1.3
     */
    public static String unquote(String s) {
	if (s.startsWith("\"") && s.endsWith("\"")) {
	    s = s.substring(1,s.length()-1);
	}
	return s;
    }

    /**
     * Check for ASCII values between [A-Z] or [a-z].
     *
     * @since 1.2
     */
    public static boolean isLetter(int c) {
	return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    /**
     * Check for ASCII values between [0-9].
     *
     * @since 1.2
     */
    public static boolean isNumber(int c) {
	return c >= 48 && c <= 57;
    }

    /**
     * Convert a char array to a byte array using UTF16 encoding.
     *
     * @since 1.2
     */
    public static byte[] toBytes(char[] chars) {
	return toBytes(chars, UTF16);
    }

    /**
     * Convert a char array to a byte array using the specified encoding. Like new String(chars).getBytes(charset), except without
     * allocating a String.
     *
     * @since 1.2
     */
    public static byte[] toBytes(char[] chars, Charset charset) {
	//
	// Perform the conversion
	//
	byte[] temp = charset.encode(CharBuffer.wrap(chars)).array();

	//
	// Terminate at the first NULL
	//
	int len = 0;
	for (int i=0; i < temp.length; i++) {
	    if (temp[i] == 0) {
		len = i;
		break;
	    } else {
		len++;
	    }
	}
	if (len == temp.length) {
	    return temp;
	} else {
	    byte[] trunc = Arrays.copyOfRange(temp, 0, len);
	    Arrays.fill(temp, (byte)0);
	    return trunc;
	}
    }

    /**
     * Convert a byte array in the specified encoding to a char array.
     *
     * @since 1.2
     */
    public static char[] toChars(byte[] bytes, Charset charset) {
	return toChars(bytes, 0, bytes.length, charset);
    }

    /**
     * Convert len bytes of the specified array in the specified encoding, starting from offset, to a char array.
     *
     * @since 1.2
     */
    public static char[] toChars(byte[] bytes, int offset, int len, Charset charset) {
	return charset.decode(ByteBuffer.wrap(bytes, offset, len)).array();
    }

    /**
     * Return the number of times ch occurs in target.
     *
     * @since 1.3
     */
    public static int countOccurrences(String target, char ch) {
	int count = 0;
	char[] chars = target.toCharArray();
	for (int i=0; i < chars.length; i++) {
	    if (chars[i] == ch) {
		count++;
	    }
	}
	return count;
    }

    /**
     * Read the contents of a File as a String, using the specified character set.
     *
     * @since 1.3.2
     */
    public static String readFile(File f, Charset charset) throws IOException {
	InputStreamReader reader = new InputStreamReader(new FileInputStream(f), charset);
	try {
	    StringBuffer buff = new StringBuffer();
	    char[] ch = new char[1024];
	    int len = 0;
	    while((len = reader.read(ch, 0, 1024)) > 0) {
		buff.append(ch, 0, len);
	    }
	    return buff.toString();
	} finally {
	    reader.close();
	}
    }

    /**
     * Determine whether or not the character at ptr is preceeded by an odd number of escape characters.
     *
     * @since 1.3.4
     */
    public static boolean isEscaped(String s, int ptr) {
	int escapes = 0;
	while (ptr-- > 0) {
	    if ('\\' == s.charAt(ptr)) {
		escapes++;
	    } else {
		break;
	    }
	}
	//
	// If the character is preceded by an even number of escapes, then it is unescaped.
	//
	if (escapes % 2 == 0) {
	    return false;
	}
	return true;
    }

    /**
     * Convert a Throwable stack trace to a String.
     *
     * @since 1.3.5
     */
    public static String toString(Throwable t) {
	StringBuffer sb = new StringBuffer(t.getClass().getName());
	sb.append(": ").append(t.getMessage() == null ? "null" : t.getMessage()).append(LF);
	StackTraceElement[] ste = t.getStackTrace();
	for (int i=0; i < ste.length; i++) {
	    sb.append("        at ").append(ste[i].toString()).append(LF);
	}
	Throwable cause = t.getCause();
	if (cause != null) {
	    sb.append("Caused by: ").append(toString(cause));
	}
	return sb.toString();
    }

    /**
     * Trim white-space from the left-hand side of a string.
     *
     * @since 1.3.8
     */
    public static String leftTrim(String s) {
	int ptr = 0;
	for (int i=0; i < s.length(); i++) {
	    switch(s.charAt(i)) {
	      case ' ':
	      case '\t':
	      case '\r':
	      case '\n':
		ptr++;
		break;
	      default:
		return s.substring(ptr);
	    }
	}
	return "";
    }

    // Private

    /**
     * Comparator implementation for Strings.
     */
    private static final class StringComparator implements Comparator<String>, Serializable {
	boolean ascending = true;

	/**
	 * @param asc Set to true for ascending, false for descending.
	 */
	StringComparator(boolean asc) {
	    this.ascending = asc;
	}

	public int compare(String s1, String s2) {
	    if (ascending) {
		return s1.compareTo(s2);
	    } else {
		return s2.compareTo(s1);
	    }
	}

	public boolean equals(Object obj) {
	    return super.equals(obj);
	}
    }

    static final class StringTokenIterator implements Iterator<String> {
	private String target, delimiter, next, last=null;
	private boolean ignoreEscaped;
	int pointer;

	StringTokenIterator(String target, String delimiter) {
	    this(target, delimiter, true);
	}

	StringTokenIterator(String target, String delimiter, boolean trim) {
	    this(target, delimiter, trim, false);
	}

	StringTokenIterator(String target, String delimiter, boolean trim, boolean ignoreEscaped) {
	    if (trim) {
		//
		// Trim tokens from the beginning and end.
		//
		int len = delimiter.length();
		while(target.startsWith(delimiter)) {
		    target = target.substring(len);
		}
		while(target.endsWith(delimiter)) {
		    if (ignoreEscaped && isEscaped(target, target.length() - len)) {
			break;
		    } else {
			target = target.substring(0, target.length() - len);
		    }
		}
	    }
	    this.target = target;
	    this.delimiter = delimiter;
	    this.ignoreEscaped = ignoreEscaped;
	    pointer = 0;
	}

	public boolean hasNext() {
	    if (next == null) {
		try {
		    next = next();
		} catch (NoSuchElementException e) {
		    return false;
		}
	    }
	    return true;
	}

	public String next() throws NoSuchElementException {
	    if (next != null) {
		String tmp = next;
		next = null;
		return tmp;
	    }
	    int i = pointer;
	    do {
		if (i > pointer) {
		    i += delimiter.length();
		}
		i = target.indexOf(delimiter, i);
	    } while (i != -1 && ignoreEscaped && isEscaped(target, i));
	    if (last != null) {
		String tmp = last;
		last = null;
		return tmp;
	    } else if (pointer >= target.length()) {
		throw new NoSuchElementException("No tokens after " + pointer);
	    } else if (i == -1) {
		String tmp = target.substring(pointer);
		pointer = target.length();
		return tmp;
	    } else {
		String tmp = target.substring(pointer, i);
		pointer = (i + delimiter.length());
		if (pointer == target.length()) {
		    // special case; append an empty token when ending with the token
		    last = "";
		}
		return tmp;
	    }
	}

	public void remove() {
	    throw new UnsupportedOperationException("Remove not supported");
	}
    }
}
