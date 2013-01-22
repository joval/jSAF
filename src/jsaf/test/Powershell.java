// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.*;

import jsaf.intf.system.ISession;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.intf.windows.powershell.IRunspacePool;
import jsaf.intf.windows.system.IWindowsSession;

public class Powershell {
    IWindowsSession session;

    public Powershell(ISession session) {
	this.session = (IWindowsSession)session;
    }

    public void test(String command) {
	try {
	    IRunspace rs = session.getRunspacePool().spawn();
	    System.out.println("Powershell prompt: " + rs.getPrompt());
	    System.out.println(rs.invoke(command));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

