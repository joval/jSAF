// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

import java.io.Console;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsaf.Message;
import jsaf.intf.identity.ICredential;
import jsaf.intf.io.IFile;
import jsaf.intf.remote.IConnectionSpecification;
import jsaf.intf.system.ISession;
import jsaf.intf.windows.identity.IWindowsCredential;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.provider.SessionFactory;

public class WinExample {
    public static void main (String[] argv) {
	ISession session = null;
	try {
	    //
	    // Hook up java.util.logging to the SLF4J logger
	    // For this to work, you must include slf4j-jdk14-1.6.2.jar in the classpath
	    //
	    Logger logger = Logger.getLogger(Message.getLogger().getName());
	    logger.setUseParentHandlers(false);
	    logger.setLevel(Level.INFO);
	    Handler handler = new ConsoleHandler();
	    handler.setFormatter(new SimpleFormatter());
	    handler.setLevel(Level.INFO);
	    logger.addHandler(handler);

	    //
	    // Create a session
	    //
	    SessionFactory factory = SessionFactory.newInstance(SessionFactory.REMOTE_FACTORY, null, null);
	    session = factory.createSession(new InteractiveTargetSpec());
	    switch(session.getType()) {
	      case WINDOWS:
		IWindowsSession ws = (IWindowsSession)session;
		if (ws.connect()) {
		    //
		    // We're connected! Let's check out the Windows Update log.
		    //
		    IFile wuLog = ws.getFilesystem().getFile("%SystemRoot%\\WindowsUpdate.log");
		    if (wuLog.exists()) {
			System.out.println("Found file: " + wuLog.getCanonicalPath());
			System.out.println("Length: " + wuLog.length() + " bytes");
			System.out.println("Last updated: " + wuLog.getLastModified().toString());
		    } else {
			System.out.println("File not found: " + wuLog.getPath());
		    }
		    ws.disconnect();
		} else {
		    System.out.println("Failed to connect!");
		}
		break;

	      default:
		//
		// This is impossible.
		//
		throw new Exception("Not a Windows session!");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (session != null) {
		session.dispose();
	    }
	}
	System.exit(0);
    }

    /**
     * The InteractiveTargetSpec inner class provides information about the host to which you'd like to
     * connect, including the credential required to connect to it.
     */
    static class InteractiveTargetSpec implements IConnectionSpecification, IWindowsCredential {
	private String hostname, domain, username;
	private char[] password;

	/**
	 * Create an IConnectionSpecification and IWindowsCredential simultaneously by
	 * querying the user from the command-line.
	 */
	InteractiveTargetSpec() throws IOException {
	    Console console = System.console();
	    hostname = console.readLine("Hostname: ");
	    String netbiosUser = console.readLine("Username ([DOMAIN\\]NAME): ");
	    int ptr = netbiosUser.indexOf("\\");
	    if (ptr == -1) {
		username = netbiosUser;
		domain = "LOCALDOMAIN";
	    } else {
		username = netbiosUser.substring(ptr+1);
		domain = netbiosUser.substring(0,ptr);
	    }
	    password = console.readPassword("Password: ");
	}

	// Implement ICredential (super-interface of IWindowsCredential

	public String getUsername() {
	    return username;
	}

	public char[] getPassword() {
	    return Arrays.copyOf(password, password.length);
	}

	// Implement IWindowsCredential

	public String getDomain() {
	    return domain;
	}

	public String getDomainUser() {
	    return domain + "\\" + username;
	}

	// Implement IConnectionSpecification

        public Type getType() {
	    return Type.WINDOWS;
        }

        public String getIdentifier() {
	    return "unique";
        }

        public String getHostname() {
	    return hostname;
        }

        public int getPort() {
	    return 5985; // WS-Management port
        }

        public String getFingerprint() {
	    return "*";
        }

        public ICredential getCredential() {
	    return this;
        }

        public IConnectionSpecification getGateway() {
	    // Support direct connections only for now
	    return null;
        }
    }
}
