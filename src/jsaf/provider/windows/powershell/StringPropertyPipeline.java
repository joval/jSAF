// Copyright (C) 2021, JovalCM.com.  All rights reserved.

package jsaf.provider.windows.powershell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jsaf.intf.windows.powershell.IPipeline;
import jsaf.util.Checksum;
import jsaf.util.Strings;

/**
 * Utility class for encoding a Powershell pipeline of Dictionary&lt;string, string&gt; mappings directed at an expression.
 */
public class StringPropertyPipeline implements IPipeline<Map<String, String>> {
    private List<Map<String, String>> members;
    private String expression;

    public StringPropertyPipeline() {
	members = new ArrayList<Map<String, String>>();
    }

    // Implement Iterable<String>

    public Iterator<Map<String, String>> iterator() {
	return members.iterator();
    }

    // Implement IPipeline<Map<String, String>>

    public int size() {
	return members.size();
    }

    public void add(Map<String, String> arg) {
	if (arg == null) {
	    throw new NullPointerException();
	}
	members.add(arg);
    }

    public void setExpression(String expression) {
	this.expression = expression;
    }

    public String getExpression() {
	return expression;
    }

    public String checksum() {
	try {
	    MessageDigest digest = digest = MessageDigest.getInstance("MD5");
	    for (int i=0; i < members.size(); i++) {
		Map<String, String> member = members.get(i);
		if (!(member instanceof TreeMap)) {
		    member = new TreeMap<String, String>(member);
		}
		for (Map.Entry<String, String> entry : member.entrySet()) {
		    digest.update(entry.getKey().getBytes(Strings.UTF8));
		    digest.update((byte)0x61); // =
		    digest.update(entry.getValue().getBytes(Strings.UTF8));
		    digest.update((byte)0x59); // ;
		}
		if (i > 0) {
		    digest.update((byte)0x00);
		}
	    }
	    digest.update(DIV);
	    digest.update(expression.getBytes(Strings.UTF8));
	    byte[] cs = digest.digest();
	    StringBuffer sb = new StringBuffer();
	    for (int i=0; i < cs.length; i++) {
	      sb.append(Integer.toString((cs[i]&0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	} catch (NoSuchAlgorithmException e) {
	    throw new RuntimeException(e);
	}
    }

    // Private

    private static final byte[] DIV = ":Expression:".getBytes(Strings.UTF8);
}
