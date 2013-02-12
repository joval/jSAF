// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.NoSuchElementException;

import jsaf.Message;

/**
 * Apparently there are still a few things that haven't yet been packed into java.lang.String!
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class StringTools {
    /**
     * Array containing all the regex special characters.
     *
     * @since 1.0
     */
    public static final char[] REGEX_CHARS = {'\\', '^', '.', '$', '|', '(', ')', '[', ']', '{', '}', '*', '+', '?'};

    /**
     * An ascending Comparator for Strings.
     *
     * @since 1.0
     */
    public static final Comparator<String> COMPARATOR = new StringComparator(true);

    /**
     * ASCII charset.
     *
     * @since 1.0
     */
    public static final Charset ASCII = Charset.forName("US-ASCII");

    /**
     * UTF8 charset.
     *
     * @since 1.0
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * UTF16 charset.
     *
     * @since 1.0
     */
    public static final Charset UTF16 = Charset.forName("UTF-16");

    /**
     * UTF16 Little Endian charset.
     *
     * @since 1.0
     */
    public static final Charset UTF16LE = Charset.forName("UTF-16LE");

    /**
     * The line separator on the local machine.
     *
     * @since 1.0
     */
    public static final String LOCAL_CR = System.getProperty("line.separator");

    /**
     * Sort the array from A->Z (ascending ordering).
     *
     * @since 1.0
     */
    public static final String[] sort(String[] array) {
	return sort(array, true);
    }

    /**
     * Arrays can be sorted ascending or descending.
     *
     * @param asc true for ascending (A->Z), false for descending (Z->A).
     *
     * @since 1.0
     */
    public static final String[] sort(String[] array, boolean asc) {
	Arrays.sort(array, new StringComparator(asc));
	return array;
    }

    /**
     * A StringTokenizer operates on single-character tokens.  This acts on a delimiter that is a multi-character
     * String.
     *
     * @since 1.0
     */
    public static Iterator<String> tokenize(String target, String delimiter) {
	return new StringTokenIterator(target, delimiter);
    }

    /**
     * Gives you an option to keep any zero-length tokens at the ends of the target, if it begins or ends with the delimiter.
     * This guarantees that you get one token for every time the delimiter appears in the target String.
     *
     * @since 1.0
     */
    public static Iterator<String> tokenize(String target, String delimiter, boolean trim) {
	return new StringTokenIterator(target, delimiter, trim);
    }

    /**
     * Convert an Iterator of Strings to a List.
     *
     * @since 1.0
     */
    public static List<String> toList(Iterator<String> iter) {
	List<String> list = new Vector<String>();
	while (iter.hasNext()) {
	    list.add(iter.next());
	}
	return list;
    }

    /**
     * Convert an array of Strings to a List.
     *
     * @since 1.0
     */
    public static List<String> toList(String[] sa) {
	List<String> list = new Vector<String>(sa.length);
	for (int i=0; i < sa.length; i++) {
	    list.add(sa[i]);
	}
	return list;
    }

    /**
     * Check for ASCII values between [A-Z] or [a-z].
     *
     * @since 1.0
     */
    public static boolean isLetter(int c) {
	return (c >= 65 && c <= 90) || (c >= 95 && c <= 122);
    }

    /**
     * Convert byte[] to char[], assuming the buffer is ASCII-encoded.
     *
     * @since 1.0
     */
    public static char[] toASCIICharArray(byte[] buff) throws IllegalArgumentException {
	char[] ca = new char[buff.length];
	for (int i=0; i < buff.length; i++) {
	    int ch = buff[i]&0xFF;
	    switch(ch) {
	      case 0x00:
	      case 0x09:
	      case 0x0A:
	      case 0x0D:
		ca[i] = (char)buff[i];
		break;

	      default:
		if ((0x20 <= ch && ch <= 0x7E)) {
		    ca[i] = (char)buff[i];
		    break;
		} else {
		    String msg = Message.getMessage(Message.ERROR_ASCII_CONVERSION, i, Byte.toString(buff[i]));
		    throw new IllegalArgumentException(msg);
		}
	    }
	}
	return ca;
    }

    /**
     * Escape any regular expression elements in the string.  This is different from Pattern.quote, which simply puts the
     * string inside of \Q...\E.
     *
     * @since 1.0
     */
    public static String escapeRegex(String s) {
	Stack<String> delims = new Stack<String>();
	for (int i=0; i < REGEX_STRS.length; i++) {
	    delims.add(REGEX_STRS[i]);
	}
	return safeEscape(delims, s);
    }

    /**
     * Returns true if the specified String contains any regular expression syntax.
     *
     * @since 1.0
     */
    public static boolean containsRegex(String s) {
	for (String ch : REGEX_STRS) {
	    if (s.indexOf(ch) != -1) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns true if the specified String contains any regular expression syntax that is not escaped.
     *
     * @since 1.0
     */
    public static boolean containsUnescapedRegex(String s) {
	for (int i=1; i < REGEX_STRS.length; i++) { // skip ESCAPE
	    int ptr = -1;
	    while ((ptr = s.indexOf(REGEX_STRS[i], ptr+1)) != -1) {
		int escapes = 0, ptr2 = ptr;
		while (ptr2-- > 0) {
		    if ('\\' == s.charAt(ptr2)) {
			escapes++;
		    } else {
			break;
		    }
		}

		//
		// If the regex character is preceded by an even number of escapes, then it is unescaped.
		//
		if (escapes % 2 == 0) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Perform a substitution of POSIX character classes to Java character classes.
     *
     * @since 1.0
     */
    public static String regexPosix2Java(String perlExpression) {
	String javaExpression = perlExpression;
	javaExpression = javaExpression.replace("[:digit:]", "\\p{Digit}");
	javaExpression = javaExpression.replace("[:alnum:]", "\\p{Alnum}");
	javaExpression = javaExpression.replace("[:alpha:]", "\\p{Alpha}");
	javaExpression = javaExpression.replace("[:blank:]", "\\p{Blank}");
	javaExpression = javaExpression.replace("[:xdigit:]","\\p{XDigit}");
	javaExpression = javaExpression.replace("[:punct:]", "\\p{Punct}");
	javaExpression = javaExpression.replace("[:print:]", "\\p{Print}");
	javaExpression = javaExpression.replace("[:space:]", "\\p{Space}");
	javaExpression = javaExpression.replace("[:graph:]", "\\p{Graph}");
	javaExpression = javaExpression.replace("[:upper:]", "\\p{Upper}");
	javaExpression = javaExpression.replace("[:lower:]", "\\p{Lower}");
	javaExpression = javaExpression.replace("[:cntrl:]", "\\p{Cntrl}");
	return javaExpression;
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

    private static final String ESCAPE = "\\";
    private static final String[] REGEX_STRS = {ESCAPE, "^", ".", "$", "|", "(", ")", "[", "]", "{", "}", "*", "+", "?"};

    private static String safeEscape(Stack<String> delims, String s) {
	if (delims.empty()) {
	    return s;
	} else {
	    String delim = delims.pop();
	    Stack<String> copy = new Stack<String>();
	    copy.addAll(delims);
	    List<String> list = StringTools.toList(StringTools.tokenize(s, delim, false));
	    int len = list.size();
	    StringBuffer result = new StringBuffer();
	    for (int i=0; i < len; i++) {
		if (i > 0) {
		    result.append(ESCAPE);
		    result.append(delim);
		}
		result.append(safeEscape(copy, list.get(i)));
	    }
	    return result.toString();
	}
    }

    static final class StringTokenIterator implements Iterator<String> {
	private String target, delimiter, next, last=null;
	int pointer;

	StringTokenIterator(String target, String delimiter) {
	    this(target, delimiter, true);
	}

	StringTokenIterator(String target, String delimiter, boolean trim) {
	    if (trim) {
		//
		// Trim tokens from the beginning and end.
		//
		int len = delimiter.length();
		if (target.startsWith(delimiter)) {
		    target = target.substring(len);
		}
		if (target.endsWith(delimiter)) {
		    target = target.substring(0, target.length() - len);
		}
	    }

	    this.target = target;
	    this.delimiter = delimiter;
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
	    int i = target.indexOf(delimiter, pointer);
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
