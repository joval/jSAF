// Copyright (C) 2019 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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
import jsaf.intf.ssh.discovery.ISshDiscoveryService;
import jsaf.intf.ssh.identity.ISshCredential;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.io.Streams;
import jsaf.provider.SessionException;
import jsaf.provider.SessionFactory;
import jsaf.util.SafeCLI;

public class LinuxExample {
    /**
     * This main method takes the following arguments (in order):
     * 1: hostname
     * 2: username
     * 3: password
     * 4: path to a PEM file
     * 5: passphrase for the PEM file
     */
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
	    // Create a RemoteSessionFactory, which also implements ISshDiscoveryService, and an IConnectionSpecification.
	    //
	    SessionFactory factory = SessionFactory.newInstance(SessionFactory.REMOTE_FACTORY, null, null);
	    String hostname = argv[0];
	    String username = argv[1];
	    char[] password = argv[2].toCharArray();
	    ByteArrayOutputStream buff = new ByteArrayOutputStream();
	    Streams.copy(new FileInputStream(argv[3]), buff);
	    char[] passphrase = argv[4].toCharArray();
	    ISshDiscoveryService service = (ISshDiscoveryService)factory;
	    byte[] decodedPem = service.decryptPrivateKey(buff.toByteArray(), passphrase);
	    IConnectionSpecification spec = new SshTargetSpec(hostname, username, password, decodedPem);

	    //
	    // Create the session
	    //
	    session = factory.createSession(spec);
	    ISession.Type type = session.getType();
	    switch(type) {
	      case UNIX:
		IUnixSession us = (IUnixSession)session;
		try {
		    us.connect();
		    //
		    // We're connected! Let's check out the distribution info.
		    //
		    System.out.println(SafeCLI.exec("uname -a", us, IUnixSession.Timeout.S));
		    us.disconnect();
		} catch (SessionException e) {
		    System.out.println("Failed to connect: " + e.getMessage());
		}
		break;

	      default:
		throw new Exception("LinuxExample does not work with session type " + type);
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
     * The SshTargetSpec inner class provides information about the Linux host to which you'd like to
     * connect, including the credentials required to connect to it.
     */
    static class SshTargetSpec implements IConnectionSpecification, ISshCredential {
	private String hostname, username;
	private char[] password;
	private byte[] decodedPem;

	/**
	 * Create a combination IConnectionSpecification/ISshCredential based on initialization parameters.
	 */
	SshTargetSpec(String hostname, String username, char[] password, byte[] decodedPem) throws IOException {
	    this.hostname = hostname;
	    this.username = username;
	    this.password = password;
	    this.decodedPem = decodedPem;
	}

	// Implement ICredential (super-interface of ISshCredential)

	public String getUsername() {
	    return username;
	}

	/**
	 * The session factory should be expected to clear the array we return after using it, so this method
	 * returns a copy.
	 */
	public char[] getPassword() {
	    return Arrays.copyOf(password, password.length);
	}

	// Implement ISshCredential

	/**
	 * Since the private key this class returns is already decrypted, this method returns null.
	 *
	 * If we returned a non-null passphrase to decrypt the private key, note that the session factory should
	 * be expected to clear the contents of the array we returned after using it.
	 */
	public char[] getPassphrase() {
	    return null;
	}

	/**
	 * The session factory should be expected to clear the array we return after using it, so this method
	 * returns a copy.
	 */
	public byte[] getPrivateKey() {
	    return Arrays.copyOf(decodedPem, decodedPem.length);
	}

	/**
	 * This example uses 'sudo' for privilege escalation. The session factory will use
	 * IConnectionSpecification.getCredential().getPassword() to get the user's password when/if prompted.
	 */
	public PrivilegeEscalation getPrivilegeEscalation() {
	    return new PrivilegeEscalation() {
		public PrivilegeEscalation.Type getType() {
		    return PrivilegeEscalation.Type.SUDO;
		}

		public ICredential getCredential() {
		    return null;
		}
	    };
	}

	// Implement IConnectionSpecification

        public Type getType() {
	    return Type.SSH;
        }

        public String getIdentifier() {
	    return "unique";
        }

        public String getHostname() {
	    return hostname;
        }

        public int getPort() {
	    return 22; // SSH port
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
