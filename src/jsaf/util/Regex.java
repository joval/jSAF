// Copyright (C) 2011-2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

  protected static final String OBRACE = "{";
  protected static final String CBRACE = "}";
  private static final String ESCAPE = "\\";
  private static final String QUALIFIER_PATTERN = "[0-9]+,{0,1}[0-9]*";

  // Characters that have special meanings within a regex for which escaping is required to match the literal character.
  private static final String[] SPECIAL_CHARACTERS = { ESCAPE, "^", ".", "$", "|", "(", ")", "[", "]", OBRACE, CBRACE, "*", "+", "?" };

  public static enum RegexConcept {
    // . - Match any character (except newline)
    DOT("."),
    // () - Grouping
    GROUPING("()"),
    // [] - Character class (also matches [^])
    CHARACTER_CLASS("[]"),
    // | - Alternation
    ALTERNATION("|"),
    // + - Match 1 or more times
    PLUS("+"),
    // * - Match 0 or more times
    STAR("*"),
    // ? - Match 1 or 0 times
    QUESTION("?"),
    // ^ - Match the beginning of the line (does not match [^])
    LINE_START("^"),
    // $ - Match the end of the line (or before newline at the end)
    LINE_END("$"),
    // \w - Match a "word" character (alphanumeric plus "_")
    WORD("\\w"),
    // \W - Match a non-word character
    NONWORD("\\W"),
    // \s - Match a whitespace character
    WHITESPACE("\\s"),
    // \S - Match a non-whitespace character
    NONWHITESPACE("\\S"),
    // \d - Match a digit character
    DIGIT("\\d"),
    // \D - Match a non-digit character
    NONDIGIT("\\D"),
    // \b - Match a word boundary
    WORD_BOUNDARY("\\b"),
    // \B - Match a non-(word boundary)
    NONWORD_BOUNDARY("\\B"),
    // {n} - Match exactly n times
    MATCH_EXACTLY("{n}"),
    // {n,} - Match at least n times
    MATCH_ATLEAST("{n,}"),
    // {n,m} - Match at least n but not more than m times
    MATCH_BETWEEN("{n,m}"),
    // \p{Lower} - A lower-case alphabetic character: [a-z]
    // \p{Upper}	An upper-case alphabetic character:[A-Z]
    // \p{ASCII}	All ASCII:[\x00-\x7F]
    // \p{Alpha}	An alphabetic character:[\p{Lower}\p{Upper}]
    // \p{Digit}	A decimal digit: [0-9]
    // \p{Alnum}	An alphanumeric character:[\p{Alpha}\p{Digit}]
    // \p{Punct}	Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
    // \p{Graph}	A visible character: [\p{Alnum}\p{Punct}]
    // \p{Print}	A printable character: [\p{Graph}\x20]
    // \p{Blank}	A space or a tab: [ \t]
    // \p{Cntrl}	A control character: [\x00-\x1F\x7F]
    // \p{XDigit}	A hexadecimal digit: [0-9a-fA-F]
    // \p{Space}	A whitespace character: [ \t\n\x0B\f\r]
    POSIX("\\p{}"),
    // Error value - No regex defined for this
    UNKNOWN("unknown");

    private String s;

    RegexConcept(String s) {
      this.s = s;
    }

    /**
     * Get the RegexConcept's corresponding String value.
     */
    public String value() {
      return s;
    }

    /**
     * Given a String value, obtain a corresponding RegexConcept.
     */
    public static RegexConcept typeOf(String s) {
      for (RegexConcept t : values()) {
        if (t.s.equals(s)) {
          return t;
        }
      }
      return UNKNOWN;
    }
  }

  static final Map<RegexConcept, Pattern> REGEX_REGEX = Collections.unmodifiableMap(new HashMap<RegexConcept, Pattern>() {
    {
      // . - Match any character (except newline)
      put(RegexConcept.DOT, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\..*"));
      // () - Grouping
      put(RegexConcept.GROUPING, Pattern.compile("(.*?(?<!\\\\)(?:\\\\{2})*)\\(.*?(?<!\\\\)(?:\\\\{2})*\\).*"));
      // [] - Character class (also matches [^])
      put(RegexConcept.CHARACTER_CLASS, Pattern.compile("(.*?(?<!\\\\)(?:\\\\{2})*)\\[\\^?.*?(?<!\\\\)(?:\\\\{2})*\\].*"));
      // | - Alternation
      put(RegexConcept.ALTERNATION, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\|(?![^\\[\\]]*\\]).*"));
      // + - Match 1 or more times
      put(RegexConcept.PLUS, Pattern.compile("((?!.*\\[.*\\\\?\\+.*\\].*$).*[^\\\\](?:\\\\{2})*)\\+.*"));
      // * - Match 0 or more times
      put(RegexConcept.STAR, Pattern.compile("((?!.*\\[.*\\\\?\\*.*\\].*$).*[^\\\\](?:\\\\{2})*)\\*.*"));
      // ? - Match 1 or 0 times
      put(RegexConcept.QUESTION, Pattern.compile("((?!.*\\[.*\\\\?\\?.*\\].*$).*[^\\\\](?:\\\\{2})*)\\?.*"));
      // ^ - Match the beginning of the line (does not match [^])
      put(RegexConcept.LINE_START, Pattern.compile("((?:|[^\\\\](?:\\\\{2})*\\^.*|(?!.*\\[.*?\\\\?\\^.*\\].*$).*[^\\\\])(?:\\\\{2})*)\\^.*"));
      // $ - Match the end of the line (or before newline at the end)
      put(RegexConcept.LINE_END, Pattern.compile("((?:|[^\\\\](?:\\\\{2})*\\$.*|(?!.*\\[.*\\\\?\\$.*\\].*$).*[^\\\\])(?:\\\\{2})*)\\$.*"));
      // \w - Match a "word" character (alphanumeric plus "_")
      put(RegexConcept.WORD, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\w.*"));
      // \W - Match a non-word character
      put(RegexConcept.NONWORD, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\W.*"));
      // \s - Match a whitespace character
      put(RegexConcept.WHITESPACE, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\s.*"));
      // \S - Match a non-whitespace character
      put(RegexConcept.NONWHITESPACE, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\S.*"));
      // \d - Match a digit character
      put(RegexConcept.DIGIT, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\d.*"));
      // \D - Match a non-digit character
      put(RegexConcept.NONDIGIT, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\D.*"));
      // \b - Match a word boundary
      put(RegexConcept.WORD_BOUNDARY, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\b.*"));
      // \B - Match a non-(word boundary)
      put(RegexConcept.NONWORD_BOUNDARY, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\B.*"));
      // {n} - Match exactly n times
      put(RegexConcept.MATCH_EXACTLY, Pattern.compile("(.*?[^\\\\](?:\\{2})*)\\{[0-9]+\\}(?![^\\[\\]]*\\]).*"));
      // {n,} - Match at least n times
      put(RegexConcept.MATCH_ATLEAST, Pattern.compile("(.*?[^\\\\](?:\\{2})*)\\{[0-9]+,\\}(?![^\\[\\]]*\\]).*"));
      // {n,m} - Match at least n but not more than m times
      put(RegexConcept.MATCH_BETWEEN, Pattern.compile("(.*?[^\\\\](?:\\{2})*)\\{[0-9]+,[0-9]+\\}(?![^\\[\\]]*\\]).*"));
      // \p{Lower} - A lower-case alphabetic character: [a-z]
      // \p{Upper}	An upper-case alphabetic character:[A-Z]
      // \p{ASCII}	All ASCII:[\x00-\x7F]
      // \p{Alpha}	An alphabetic character:[\p{Lower}\p{Upper}]
      // \p{Digit}	A decimal digit: [0-9]
      // \p{Alnum}	An alphanumeric character:[\p{Alpha}\p{Digit}]
      // \p{Punct}	Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
      // \p{Graph}	A visible character: [\p{Alnum}\p{Punct}]
      // \p{Print}	A printable character: [\p{Graph}\x20]
      // \p{Blank}	A space or a tab: [ \t]
      // \p{Cntrl}	A control character: [\x00-\x1F\x7F]
      // \p{XDigit}	A hexadecimal digit: [0-9a-fA-F]
      // \p{Space}	A whitespace character: [ \t\n\x0B\f\r]
      put(RegexConcept.POSIX, Pattern.compile("(.*?(?<!\\\\)(?:\\\\\\\\)*)\\\\p\\{(Lower|Upper|ASCII|Alpha|Digit|Alnum|Punct|Graph|Print|Blank|Cntrl|XDigit|Space)\\}.*"));
    }
  });

  /**
   * Escape any regular expression elements in the string.  This is different from Pattern.quote, which simply puts the
   * string inside of \Q...\E.
   */
  public static String escapeRegex(String s) {
    return safeEscape(s, SPECIAL_CHARACTERS);
  }

  /**
   * Undo escapeRegex.
   */
  public static String unescapeRegex(String s) {
    return safeUnescape(s, SPECIAL_CHARACTERS);
  }

  /**
   * Returns true if the specified String contains any special regular expression characters.
   */
  public static boolean containsSpecialCharacters(String s) {
    for (String ch : SPECIAL_CHARACTERS) {
      if (s.indexOf(ch) != -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the longest prefix in the pattern string for which there is no regex expression characters.
   * @param pattern The regex pattern.
   * @param excludedConcepts Regex patterns to exclude.
   * @return The longest prefix in the pattern string for which there is no regex expression characters.
   */
  public static String findLongestNonRegexPrefix(Pattern pattern, RegexConcept... excludedConcepts) {
    String s = pattern.pattern();
    if (s.equals("")) {
      return s;
    }
    List<RegexConcept> concepts = new ArrayList<RegexConcept>(REGEX_REGEX.keySet());
    for (RegexConcept regexConcept : excludedConcepts) {
      concepts.remove(regexConcept);
    }
    String subString = s;
    for (RegexConcept regexConcept : concepts) {
      Matcher matcher = REGEX_REGEX.get(regexConcept).matcher(s);
      if (matcher.matches()) {
        String temp = matcher.group(1);
        if (regexConcept == RegexConcept.STAR || regexConcept == RegexConcept.PLUS || regexConcept == RegexConcept.QUESTION) {
          if (temp.length() - 1 < subString.length()) {
            subString = temp.substring(0, temp.length() - 1);
          }
        } else if (temp.length() < subString.length()) {
          subString = temp;
        }
      }
    }
    return subString;
  }

  /**
   * Splits a string pattern into multiple string patterns based on the regex alternation ('|') character (expands out a single regex into all possible alternations).
   * @param pattern The regex pattern
   * @return A list of alternations for the regex pattern
   */
  public static List<String> getAlternations(Pattern pattern) {
    if (pattern == null) {
      return null;
    }
    List<String> regexPatterns = new ArrayList<String>();
    for (StringBuilder sb : getAlternationsHelper(pattern)) {
      regexPatterns.add(sb.toString());
    }
    return regexPatterns;
  }

  private static List<StringBuilder> getAlternationsHelper(Pattern regexPattern) {
    String regexPatternString = regexPattern.pattern();
    List<StringBuilder> alts = new ArrayList<StringBuilder>();
    if (regexPatternString.length() == 0) {
      return alts;
    }
    StringBuilder sb = new StringBuilder();
    char[] chars = regexPatternString.toCharArray();
    boolean escaped = false;
    boolean hasAlts = false;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (escaped) {
        escaped = false;
        sb.append('\\');
        sb.append(c);
      } else {
        if (c == '[') {
          sb.append(c);
          i++;
          boolean firstCaptureCharFlag = true;  // Flag used to indicate parsing of the first character in the character class.  Reason for this is because if "]" is the first character, it isn't interpreted as the closing character class character.
          for (; i < chars.length; i++) {
            c = chars[i];
            if (escaped) {
                escaped = false;
                sb.append('\\');
                sb.append(c);
            } else {
              if (c == '\\') {
                escaped = true;
                firstCaptureCharFlag = false;
              } else if (c == ']' && firstCaptureCharFlag == false) {
                sb.append(c);
                break;
              } else {
                if (firstCaptureCharFlag && c != '^') {
                  firstCaptureCharFlag = false;
                }
                escaped = false;
                sb.append(c);
              }
            }
          }
        } else if (c == '{') {
          for (; c != '}' && i < chars.length; i++) {
            c = chars[i];
            sb.append(c);
          }
          i--;
        } else if (c == '\\') {
          escaped = !escaped;
        } else if (c == '(') {
          // Find closing ')'
          int count = 0;
          for (int j = i + 1; j < chars.length; j++) {
            char subC = chars[j];
            if (subC == '\\') {
              j++;
            } else if (subC == '(') {
              count++;
            } else if (subC == ')') {
              if (count == 0) {
                // a|b|(c)
                List<StringBuilder> list = getAlternationsHelper(Pattern.compile(regexPatternString.substring(i + 1, j)));
                int currentAltSize = alts.size();
                List<StringBuilder> newAlts = new ArrayList<StringBuilder>();
                for (StringBuilder sBuilder : list) {
                  StringBuilder sb2 = new StringBuilder();
                  sb2.append(sb);
                  sb2.append('(');
                  sb2.append(sBuilder);
                  sb2.append(')');
                  if (list.size() == 1) {
                    sb = sb2;
                  } else {
                    int index = i - sb.length() - 1;
                    if (currentAltSize == 0 || (index >= 0 && '|' == chars[index])) {
                      alts.add(sb2);
                    } else if (currentAltSize > 0) {
                      for (int k = 0; k < currentAltSize; k++) {
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(alts.get(k));
                        sb3.append(sb2);
                        newAlts.add(sb3);
                      }
                    } else {
                      newAlts.add(sb2);
                    }
                  }
                }
                if (newAlts.size() > 0) {
                  alts = newAlts;
                  sb = new StringBuilder();
                } else if (currentAltSize != alts.size()) {
                  sb = new StringBuilder();
                }
                i = j;
                j = chars.length;
              } else {
                count--;
              }
            }
          }
        } else if (c == '|') {
          if (sb.length() > 0) {
            hasAlts = true;
            alts.add(sb);
            sb = new StringBuilder();
          }
        } else {
          sb.append(c);
        }
      }
    }
    if (sb.length() > 0) {
      if (alts.size() == 0 || hasAlts) {
        alts.add(sb);
      } else {
        for (StringBuilder sb2 : alts) {
          sb2.append(sb);
        }
      }
    }

    return alts;
  }

  /**
   * Returns the string with the trailing end of line character, '$', removed, if not escaped.
   * @param regex Regex string.
   * @return The string with the trailing end of line character, '$', removed, if not escaped.
   */
  public static String stripMatchEndOfTheLineChar(String regex) {
    if (regex.endsWith("$")) {
      if (!Strings.isEscaped(regex, regex.length() - 1)) {
        regex = regex.substring(0, regex.length() - 1);
      }
    }
    return regex;
  }

  /**
   * Returns true if the specified String contains any regular expression syntax that is not escaped.
   */
  public static boolean containsUnescapedRegex(String pattern) throws PatternSyntaxException {
    if (pattern.equals("")) {
      return false;
    }
    for (Pattern p : REGEX_REGEX.values()) {
      Matcher matcher = p.matcher(pattern);
      if (matcher.matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the specified regex expression contains the specified regular expression syntax.  This function take into account escaped regex characters.
   * Also, returns false if there is no regex pattern defined for the RegexConcept.
   * */
  public static boolean containsSpecificRegex(RegexConcept regex, String pattern) throws PatternSyntaxException {
    Pattern p = REGEX_REGEX.get(regex);
    return p != null && p.matcher(pattern).matches();
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
   *
   * Note, for compatibility with Perl regex behavior, the UNIX_LINES flag is always enabled. Therefore, to mimic Perl
   * behavior when doing pattern matching against (text) file content, you must normalize line-breaks to Unix-style before
   * matching with the pattern.
   */
  public static Pattern pattern(String regex, int flags) throws PatternSyntaxException {
    return Pattern.compile(posix2Java(regex), flags | Pattern.UNIX_LINES);
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
    int next = pcre.indexOf(OBRACE);
    if (next == -1) {
      sb.append(escapeUnescaped(pcre, CBRACE));
    } else {
      do {
        sb.append(escapeUnescaped(pcre.substring(start, next), CBRACE));
        if (Strings.isEscaped(pcre, next)) {
          sb.append(OBRACE);
          start = next + 1;
        } else {
          int p2 = pcre.indexOf(CBRACE, next);
          if (p2 == -1) {
            sb.append(escapeUnescaped(pcre.substring(next), OBRACE));
            start = pcre.length();
          } else {
            if (Pattern.matches(QUALIFIER_PATTERN, pcre.substring(next + 1, p2))) {
              // Qualifier
              sb.append(pcre.substring(next, p2 + 1));
              start = p2 + 1;
            } else if (next > 1 && !Strings.isEscaped(pcre, next - 2) && pcre.substring(next - 2, next).equals("\\p")) {
              // Java character class
              sb.append(pcre.substring(next, p2 + 1));
              start = p2 + 1;
            } else {
              sb.append("\\").append(OBRACE);
              start = next + 1;
            }
          }
        }
      } while ((next = pcre.indexOf(OBRACE, start)) != -1);
      sb.append(escapeUnescaped(pcre.substring(start), CBRACE));
    }
    String jcre = sb.toString();

    jcre = jcre.replace("[:digit:]", "\\p{Digit}");
    jcre = jcre.replace("[:alnum:]", "\\p{Alnum}");
    jcre = jcre.replace("[:alpha:]", "\\p{Alpha}");
    jcre = jcre.replace("[:blank:]", "\\p{Blank}");
    jcre = jcre.replace("[:xdigit:]", "\\p{XDigit}");
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
    psExpression = psExpression.replace("[:xdigit:]", "[a-fA-F0-9]");
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
      s = s.substring(0, s.length() - 1);
    } else if (!s.endsWith(".*")) {
      s = new StringBuffer(s).append(".*").toString(); // trailing .* is implied
    }
    StringBuffer outerSb = new StringBuffer();
    Iterator<String> outerIter = Strings.tokenize(s, ".*", false);
    for (int i = 0; outerIter.hasNext(); i++) {
      if (i > 0) {
        outerSb.append("*");
      }
      String outerFrag = outerIter.next();
      if (outerFrag.length() > 0) {
        StringBuffer innerSb = new StringBuffer();
        Iterator<String> innerIter = Strings.tokenize(outerFrag, "\\.", false);
        for (int j = 0; innerIter.hasNext(); j++) {
          if (j > 0) {
            innerSb.append(".");
          }
          String innerFrag = innerIter.next();
          if (containsSpecialCharacters(innerFrag)) {
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
    for (int i = 0; i < delims.length; i++) {
      String delim = delims[i];
      List<String> list = Strings.toList(Strings.tokenize(s, delim, false));
      StringBuffer escaped = new StringBuffer();
      for (int j = 0; j < list.size(); j++) {
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
    for (int i = 0; i < delims.length; i++) {
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
