// Copyright (C) 2020, JovalCM.com.  All rights reserved.

package jsaf.provider.windows.powershell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsaf.intf.windows.powershell.IPipeline;

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

    public void add(String arg) {
	members.add(arg);
    }

    public void setExpression(String expression) {
	this.expression = expression;
    }

    public String getExpression() {
	return expression;
    }
}
