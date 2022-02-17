// Copyright (C) 2020, JovalCM.com.  All rights reserved.

package jsaf.provider.windows.powershell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jsaf.intf.windows.powershell.IPipeline;
import jsaf.util.Bytes;
import jsaf.util.Checksum;
import jsaf.util.Strings;

/**
 * Utility class for encoding a Powershell pipeline of Strings directed at an expression.
 */
public class StringPipeline implements IPipeline<String> {
    private Collection<String> members;
    private String expression;

    /**
     * Create a StringPipeline with an empty ArrayList argument set.
     */
    public StringPipeline() {
	members = new ArrayList<String>();
    }

    /**
     * Create a StringPipeline using the specified argument collection. The collection can be chosen
     * to appropriately solve de-duplication and ordering problems (e.g., Should duplicate arguments
     * be consolidated?  Should arguments be sorted?).
     */
    public StringPipeline(Collection<String> argCollection) {
	members = argCollection;
    }

    // Implement Iterable<String>

    public Iterator<String> iterator() {
	return members.iterator();
    }

    // Implement IPipeline<String>

    public int size() {
	return members.size();
    }

    public void add(String arg) {
	if (arg == null) {
	    throw new NullPointerException();
	}
	members.add(arg);
    }

    public void addAll(Collection<String> args) {
	members.addAll(args);
    }

    public void setExpression(String expression) {
	this.expression = expression;
    }

    public String getExpression() {
	return expression;
    }

    public String checksum() {
	try {
	    MessageDigest digest = MessageDigest.getInstance("MD5");
	    for (String arg : members) {
		digest.update(arg.getBytes(Strings.UTF8));
		digest.update((byte)0x00);
	    }
	    digest.update(DIV);
	    digest.update(expression.getBytes(Strings.UTF8));
	    return Bytes.toHexString(digest.digest());
	} catch (NoSuchAlgorithmException e) {
	    throw new RuntimeException(e);
	}
    }

    // Private

    private static final byte[] DIV = ":Expression:".getBytes(Strings.UTF8);
}
