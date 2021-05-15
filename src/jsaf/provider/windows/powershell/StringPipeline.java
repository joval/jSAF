// Copyright (C) 2020, JovalCM.com.  All rights reserved.

package jsaf.provider.windows.powershell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jsaf.intf.windows.powershell.IPipeline;
import jsaf.util.Checksum;
import jsaf.util.Strings;

public class StringPipeline implements IPipeline<String> {
    private List<String> members;
    private String expression;

    public StringPipeline() {
	members = new ArrayList<String>();
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

    public void setExpression(String expression) {
	this.expression = expression;
    }

    public String getExpression() {
	return expression;
    }

    public String getMD5() {
	try {
	    MessageDigest digest = digest = MessageDigest.getInstance("MD5");
	    for (int i=0; i < members.size(); i++) {
		digest.update(members.get(i).getBytes(Strings.UTF8));
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
