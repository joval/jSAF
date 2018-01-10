// Copyright (C) 2011-2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regular expression utilities.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.3.8
 */
public class Regex {
    protected static final char cOBRACE = '{';
    protected static final char cCBRACE = '}';
    protected static final char cOPEREN = '(';
    protected static final char cCPEREN = ')';
    protected static final char cOBRACK = '[';
    protected static final char cCBRACK = ']';

    protected static final String szOBRACE = "{";
    protected static final String szCBRACE = "}";
    protected static final String szOPEREN = "(";
    protected static final String szCPEREN = ")";
    protected static final String szOBRACK = "[";
    protected static final String szCBRACK = "]";

    private static final String ESCAPE = "\\";
    private static final String QUALIFIER_PATTERN = "[0-9]+,{0,1}[0-9]*";

    private static final char[] REGEX_CHARS = {'\\', '^', '.', '$', '|', cOPEREN, cCPEREN, cOBRACK, cCBRACK, cOBRACE, cCBRACE, '*', '+', '?'};
    private static final String[] REGEX_STRS = {ESCAPE, "^", ".", "$", "|", szOPEREN, szCPEREN, szOBRACK, szCBRACK, szOBRACE, szCBRACE, "*", "+", "?"};

    /**
     * Escape any regular expression elements in the string.  This is different from Pattern.quote, which simply puts the
     * string inside of \Q...\E.
     */
    public static String escapeRegex(String s) {
	return safeEscape(s, REGEX_STRS);
    }

    /**
     * Undo escapeRegex.
     */
    public static String unescapeRegex(String s) {
	return safeUnescape(s, REGEX_STRS);
    }

    /**
     * Returns true if the specified String contains any regular expression syntax.
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
     * Returns true if the specified character is a special regular expression character.
     */
    public static boolean isRegexChar(char c) {
	for (char ch : REGEX_CHARS) {
	    if (c == ch) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns true if the specified String contains any regular expression syntax that is not escaped.
     */
    public static boolean containsUnescapedRegex(String s) {
	for (int i=1; i < REGEX_STRS.length; i++) { // skip ESCAPE
	    int ptr = -1;
	    while ((ptr = s.indexOf(REGEX_STRS[i], ptr+1)) != -1) {
		if (!Strings.isEscaped(s, ptr)) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Compiles a Perl-style regular expression with POSIX-style character classes into a Java regular expression.
     */
    public static Pattern pattern(String regex) throws PatternSyntaxException {
	return pattern(regex, 0);
    }

    /**
     * Compiles a Perl-style regular expression with POSIX-style character classes into a Java regular expression, with
     * the specified flags (from java.util.regex.Pattern).
     */
    public static Pattern pattern(String regex, int flags) throws PatternSyntaxException {
	return Pattern.compile(posix2Java(regex), flags);
    }

    /**
     * Perform a substitution of POSIX character classes to Java character classes.
     */
    public static String posix2Java(String pcre) {
	//
	// Escape all curly-brackets that are not:
	// 1) part of a Java character class
	// 2) part of a qualifier
	// 3) already escaped
	//
	StringBuffer sb = new StringBuffer();
	int start = 0;
	int next = pcre.indexOf(szOBRACE);
	if (next == -1) {
	    sb.append(escapeUnescaped(pcre, szCBRACE));
	} else {
	    do {
		sb.append(escapeUnescaped(pcre.substring(start, next), szCBRACE));
		if (Strings.isEscaped(pcre, next)) {
		    sb.append(szOBRACE);
		    start = next+1;
		} else {
		    int p2 = pcre.indexOf(szCBRACE, next);
		    if (p2 == -1) {
			sb.append(escapeUnescaped(pcre.substring(next), szOBRACE));
			start = pcre.length();
		    } else {
			if (Pattern.matches(QUALIFIER_PATTERN, pcre.substring(next+1, p2))) {
			    // Qualifier
			    sb.append(pcre.substring(next, p2+1));
			    start = p2+1;
			} else if (next > 1 && !Strings.isEscaped(pcre,next-2) && pcre.substring(next-2,next).equals("\\p")) {
			    // Java character class
			    sb.append(pcre.substring(next, p2+1));
			    start = p2+1;
			} else {
			    sb.append("\\").append(szOBRACE);
			    start = next+1;
			}
		    } 
		} 
	    } while((next = pcre.indexOf(szOBRACE, start)) != -1);
	    sb.append(escapeUnescaped(pcre.substring(start), szCBRACE));
	}
	String jcre = sb.toString();

	jcre = jcre.replace("[:digit:]", "\\p{Digit}");
	jcre = jcre.replace("[:alnum:]", "\\p{Alnum}");
	jcre = jcre.replace("[:alpha:]", "\\p{Alpha}");
	jcre = jcre.replace("[:blank:]", "\\p{Blank}");
	jcre = jcre.replace("[:xdigit:]","\\p{XDigit}");
	jcre = jcre.replace("[:punct:]", "\\p{Punct}");
	jcre = jcre.replace("[:print:]", "\\p{Print}");
	jcre = jcre.replace("[:space:]", "\\p{Space}");
	jcre = jcre.replace("[:graph:]", "\\p{Graph}");
	jcre = jcre.replace("[:upper:]", "\\p{Upper}");
	jcre = jcre.replace("[:lower:]", "\\p{Lower}");
	jcre = jcre.replace("[:cntrl:]", "\\p{Cntrl}");
	return jcre;
    }

    /**
     * Perform a substitution of POSIX character classes to Unicode character classes. Also, replaces '\_' with '_',
     * which is a harmless error in most regular expression engines, but not Microsoft's.
     */
    public static String posix2Powershell(String pcre) {
	String psExpression = pcre;
	psExpression = psExpression.replace("[:digit:]", "\\d");
	psExpression = psExpression.replace("[:alnum:]", "\\p{L}\\p{Nd}");
	psExpression = psExpression.replace("[:alpha:]", "\\p{L}");
	psExpression = psExpression.replace("[:blank:]", "\\p{Zs}\\t");
	psExpression = psExpression.replace("[:xdigit:]","a-fA-F0-9");
	psExpression = psExpression.replace("[:punct:]", "\\p{P}");
	psExpression = psExpression.replace("[:print:]", "\\P{C}");
	psExpression = psExpression.replace("[:space:]", "\\s");
	psExpression = psExpression.replace("[:graph:]", "\\P{Z}\\P{C}");
	psExpression = psExpression.replace("[:upper:]", "\\p{Lu}");
	psExpression = psExpression.replace("[:lower:]", "\\p{Ll}");
	psExpression = psExpression.replace("[:cntrl:]", "\\p{Cc}");
	return safeUnescape(psExpression, "_");
    }

    /**
     * Attempt to convert a regex Pattern into a glob.
     *
     * @throws IllegalArgumentException if the pattern cannot be converted to a glob
     */
    public static String toGlob(Pattern p) throws IllegalArgumentException {
	String s = p.pattern();
	if (s.startsWith("^")) {
	    s = s.substring(1);
	} else if (!s.startsWith(".*")) {
	    throw new IllegalArgumentException();
	}
	if (s.endsWith("$")) {
	    s = s.substring(0, s.length()-1);
	} else if (!s.endsWith(".*")) {
	    s = new StringBuffer(s).append(".*").toString(); // trailing .* is implied
	}
	StringBuffer outerSb = new StringBuffer();
	Iterator<String> outerIter = Strings.tokenize(s, ".*", false);
	for (int i=0; outerIter.hasNext(); i++) {
	    if (i > 0) {
		outerSb.append("*");
	    }
	    String outerFrag = outerIter.next();
	    if (outerFrag.length() > 0) {
		StringBuffer innerSb = new StringBuffer();
		Iterator<String> innerIter = Strings.tokenize(outerFrag, "\\.", false);
		for (int j=0; innerIter.hasNext(); j++) {
		    if (j > 0) {
			innerSb.append(".");
		    }
		    String innerFrag = innerIter.next();
		    if (containsRegex(innerFrag)) {
			throw new IllegalArgumentException("contains regex: " + innerFrag);
		    } else {
			innerSb.append(innerFrag);
		    }
		}
		outerSb.append(innerSb.toString());
	    }
	}
	return outerSb.toString();
    }

    // Private

    /**
     * Escape unescaped instances of the specified pattern in s.
     */
    private static String escapeUnescaped(String s, String pattern) {
	StringBuffer sb = new StringBuffer();
	int last = 0;
	int next = 0;
	while ((next = s.indexOf(pattern, last)) != -1) {
	    sb.append(s.substring(last, next));
	    if (Strings.isEscaped(s, next)) {
		sb.append(pattern);
	    } else {
		sb.append(ESCAPE).append(pattern);
	    }
	    last = next + pattern.length();
	}
	return sb.append(s.substring(last)).toString();
    }

    /**
     * Escape instances of the pattern in s which are not already escaped.
     */
    private static String safeEscape(String s, String... delims) {
	//
	// Insure ESCAPE is processed first
	//
	List<String> array = new ArrayList<String>(Arrays.<String>asList(delims));
	if (array.contains(ESCAPE) && !ESCAPE.equals(delims[0])) {
	    array.remove(ESCAPE);
	    List<String> temp = array;
	    array = new ArrayList<String>();
	    array.add(ESCAPE);
	    array.addAll(temp);
	    delims = array.<String>toArray(new String[array.size()]);
	}
	for (int i=0; i < delims.length; i++) {
	    String delim = delims[i];
	    List<String> list = Strings.toList(Strings.tokenize(s, delim, false));
	    StringBuffer escaped = new StringBuffer();
	    for (int j=0; j < list.size(); j++) {
		if (j > 0) {
		    escaped.append(ESCAPE);
		    escaped.append(delim);
		}
		escaped.append(list.get(j));
	    }
	    s = escaped.toString();
	}
	return s;
    }

    /**
     * Unescape the specified stack (of escaped delimiters) from the supplied String, s. Escaped
     * delimiters are unescaped in the order provided.
     */
    private static String safeUnescape(String s, String... delims) {
	//
	// Insure ESCAPE is processed last
	//
	List<String> array = new ArrayList<String>(Arrays.<String>asList(delims));
	int lastIndex = delims.length - 1;
	if (array.contains(ESCAPE) && !ESCAPE.equals(delims[lastIndex])) {
	    array.remove(ESCAPE);
	    List<String> temp = array;
	    array = new ArrayList<String>();
	    array.addAll(temp);
	    array.add(ESCAPE);
	    delims = array.<String>toArray(new String[array.size()]);
	}
	for (int i=0; i < delims.length; i++) {
	    String delim = ESCAPE + delims[i];
	    StringBuffer unescaped = new StringBuffer();
	    int last = 0;
	    int ptr = s.indexOf(delim);
	    while (ptr != -1) {
		unescaped.append(s.substring(last, ptr));
		if (Strings.isEscaped(s, ptr)) {
		    unescaped.append(delim);
		} else {
		    unescaped.append(delim.substring(1));
		}
		last = ptr + delim.length();
		ptr = s.indexOf(delim, last);
	    }
	    unescaped.append(s.substring(last));
	    s = unescaped.toString();
	}
	return s;
    }
}
