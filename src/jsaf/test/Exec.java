// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.io.InputStream;

import jsaf.intf.system.IProcess;
import jsaf.intf.system.ISession;

public class Exec {
    ISession session;

    public Exec(ISession session) {
	this.session = session;
    }

    public void test(String command) {
	try {
	    String[] env = {"DAS=jOVAL"};
	    IProcess p = session.createProcess(command, env, null);
	    p.start();

	    InputStream in = p.getInputStream();
	    int len = 0;
	    byte[] buff = new byte[1024];
	    while((len = in.read(buff)) > 0) {
		System.out.write(buff, 0, len);
	    }
	    p.waitFor(0);
	    System.out.println("exit code: " + p.exitValue());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
