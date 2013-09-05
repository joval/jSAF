// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import jsaf.JSAFSystem;
import jsaf.Message;
import jsaf.intf.system.ISession;
import jsaf.provider.SessionFactory;

public class Default {
    public static void main (String[] argv) {
	try {
	    Properties props = new Properties();
	    if (argv.length == 1) {
		File f = new File(argv[0]);
		FileInputStream in = new FileInputStream(f);
		props.load(in);
		in.close();
	    } else {
		System.exit(1);
	    }

	    if ("true".equals(props.getProperty("verbose"))) {
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new ConsoleFormatter());
		consoleHandler.setLevel(Level.FINEST);
		Logger.getLogger(Message.getLogger().getName()).addHandler(consoleHandler);
	    }

	    ISession session = (ISession)SessionFactory.newInstance(JSAFSystem.getDataDirectory()).createSession();
	    if (session.connect()) {
		if ("true".equals(props.getProperty("test.ad"))) {
		    new AD(session).test(props.getProperty("ad.user"));
		}
		if ("true".equals(props.getProperty("test.registry"))) {
		    new Reg(session).test(props.getProperty("registry.key"), props.getProperty("registry.value"));
		}
		if ("true".equals(props.getProperty("test.powershell"))) {
		    new Powershell(session).test(props.getProperty("powershell.command"));
		}
		if ("true".equals(props.getProperty("test.exec"))) {
		    new Exec(session).test(props.getProperty("exec.command"));
		}
		if ("true".equals(props.getProperty("test.fs"))) {
		    new FS(session).test(props.getProperty("fs.path"));
		}
		if ("true".equals(props.getProperty("test.wmi"))) {
		    new WMI(session).test(props.getProperty("wmi.namespace"), props.getProperty("wmi.query"));
		}
		session.disconnect();
	    }
	    session.dispose();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	System.exit(0);
    }

    private static class ConsoleFormatter extends Formatter {
        public String format(LogRecord record) {
            StringBuffer line = new StringBuffer(record.getMessage());
            line.append('\n');
            return line.toString();
        }
    }
}

