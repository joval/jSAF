// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.identity;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jsaf.Message;

/**
 * Representation of and tools relating to Windows Security Identifiers (SIDs).
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.5.0
 */
public class SID {
    private static final Pattern SID_PATTERN;
    static {
	try {
	    SID_PATTERN = Pattern.compile("^S(-\\d+){3,}$");
	} catch (PatternSyntaxException e) {
	    throw new RuntimeException(e);
	}
    }

    public static final boolean isSidString(String s) {
	return SID_PATTERN.matcher(s).find();
    }

    /**
     * Create a SID from a String.
     *
     * @throws IllegalArgumentException if the sid value does not match the SID pattern.
     */
    public static final SID create(String sid) throws IllegalArgumentException {
	return new SID(sid);
    }

    /**
     * @return true if the SID instance corresponds to a Windows service.
     */
    public final boolean isService() {
	return value.startsWith("S-1-5-80-");
    }

    @Override
    public boolean equals(Object other) {
	if (other instanceof SID) {
	    return ((SID)other).value.equals(value);
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	return value.hashCode();
    }

    @Override
    public String toString() {
	return value;
    }

    // Private

    private final String value;

    private SID(String sid) {
	sid = sid.toUpperCase().trim();
	if (isSidString(sid)) {
	    value = sid;
	} else {
	    throw new IllegalArgumentException(Message.getMessage(Message.ERROR_SID, sid));
	}
    }
}
