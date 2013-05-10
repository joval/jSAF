// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.identity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Hashtable;
import java.util.Properties;

import jsaf.Message;
import jsaf.intf.identity.ICredential;
import jsaf.intf.identity.ICredentialStore;
import jsaf.intf.ssh.identity.ISshCredential;
import jsaf.intf.system.ISession;
import jsaf.intf.util.IProperty;
import jsaf.intf.windows.identity.IWindowsCredential;
import jsaf.io.StreamTool;
import jsaf.util.PropertyUtil;

/**
 * Trivial implementation of an ICredentialStore that contains credentials keyed by hostname.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SimpleCredentialStore implements ICredentialStore {
    public static final String PROP_HOSTNAME		= "hostname";
    public static final String PROP_DOMAIN		= "nt.domain";
    public static final String PROP_USERNAME		= "user.name";
    public static final String PROP_PASSWORD		= "user.password";
    public static final String PROP_PASSPHRASE		= "key.password";
    public static final String PROP_ROOT_PASSWORD	= "root.password";
    public static final String PROP_PRIVATE_KEY		= "key.file";

    private Hashtable<String, IProperty> table;

    /**
     * Create from Properties.
     */
    public SimpleCredentialStore() {
	table = new Hashtable<String, IProperty>();
    }

    /**
     * Add properties for a credential from an IProperty.
     *
     * @throws IllegalArgumentException if PROP_HOSTNAME, PROP_USERNAME and either PROP_PASSWORD or PROP_PRIVATE_KEY
     *         are not all specified.
     */
    public void add(IProperty prop) throws IllegalArgumentException {
	String hostname = prop.getProperty(PROP_HOSTNAME);
	if (hostname == null) {
	    throw new IllegalArgumentException(PROP_HOSTNAME);
	}
	if (prop.getProperty(PROP_USERNAME) == null) {
	    throw new IllegalArgumentException(PROP_USERNAME);
	}
	if (prop.getProperty(PROP_PASSWORD) == null && prop.getProperty(PROP_PRIVATE_KEY) == null) {
	    throw new IllegalArgumentException(PROP_PASSWORD);
	}
	table.put(hostname, prop);
    }

    /**
     * Add properties for a credential from a java.util.Properties.
     *
     * @throws IllegalArgumentException if a PROP_HOSTNAME is not specified.
     */
    public void add(Properties props) throws IllegalArgumentException {
	add(new PropertyUtil(props));
    }

    // Implement ICredentialStore

    public ICredential getCredential(ISession session) {
	return getCredential(session.getHostname());
    }

    public ICredential getCredential(String identifier) {
	IProperty prop = table.get(identifier);
	if (prop == null) {
	    return null;
	}
	String domain = prop.getProperty(PROP_DOMAIN);
	String username = prop.getProperty(PROP_USERNAME);
	char[] password = prop.containsKey(PROP_PASSWORD) ? prop.getProperty(PROP_PASSWORD).toCharArray() : null;
	char[] passphrase = prop.containsKey(PROP_PASSPHRASE) ? prop.getProperty(PROP_PASSPHRASE).toCharArray() : null;
	char[] rootPassword = prop.containsKey(PROP_ROOT_PASSWORD) ? prop.getProperty(PROP_ROOT_PASSWORD).toCharArray() : null;
	String privateKey = prop.getProperty(PROP_PRIVATE_KEY);

	ICredential cred = null;
	if (identifier.equalsIgnoreCase(prop.getProperty(PROP_HOSTNAME))) {
	    if (domain != null) {
		cred = new WindowsCredential(domain, username, password);
	    } else {
		File pkf = null;
		if (privateKey != null && (pkf = new File(privateKey)).exists()) {
		    cred = new SshCredential(username, pkf, passphrase, rootPassword);
		} else if (rootPassword != null) {
		    cred = new SshCredential(username, password, rootPassword);
		} else if (username != null && password != null) {
		    cred = new Credential(username, password);
		} else {
		    Message.getLogger().warn(Message.ERROR_SESSION_CREDENTIAL_PASSWORD, username);
		}
	    }
	}
	return cred;
    }

    // Inner Classes

    /**
     * A representation of an abstract credential, consisting of a username and password.  Subclasses include WindowsCredential
     * (which adds a DOMAIN), and SSHCredential (which adds a root password, private key file and key file passphrase).
     */
    public class Credential implements ICredential {
	protected String username;
	protected char[] password;
    
	public Credential() {
	    username = null;
	    password = null;
	}
    
	/**
	 * Create a Credential using a username and password.
	 */
	public Credential(String username, char[] password) {
	    this.username = username;
	    this.password = password;
	}
    
	public void setUsername(String username) {
	    this.username = username;
	}
    
	public void setPassword(char[] password) {
	    this.password = password;
	}
    
	// Implement ICredential
    
	public String getUsername() {
	    return username;
	}
    
	public char[] getPassword() {
	    return password;
	}
    }

    /**
     * A representation of a Unix credential.
     */
    public class SshCredential extends Credential implements ISshCredential {
	private char[] passphrase, rootPassword;
	private byte[] privateKey;

	public SshCredential(String username, char[] password, char[] rootPassword) {
	    super(username, password);
	    this.rootPassword = rootPassword;
	}

	/**
	 * Create a Credential for a certificate.
	 */
	public SshCredential(String username, File file, char[] passphrase, char[] rootPassword) {
	    this(username, null, rootPassword);
	    this.passphrase = passphrase;
	    InputStream in = null;
	    try {
		privateKey = new byte[(int)file.length()];
		in = new FileInputStream(file);
		StreamTool.readFully(in, privateKey);
	    } catch (IOException e) {
		privateKey = null;
		Message.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (IOException e) {
		    }
		}
	    }
	}

	// Implement ISshCredential

	public ICredential getRootCredential() {
	    if (rootPassword == null) {
		return null;
	    } else {
		return new Credential("root", rootPassword);
	    }
	}

	public char[] getPassphrase() {
	    return passphrase;
	}

	public byte[] getPrivateKey() {
	    return privateKey;
	}
    }

    /**
     * A representation of a Windows domain credential.
     */
    public class WindowsCredential extends Credential implements IWindowsCredential {
	private String domain;

	/**
	 * Create a Credential from a String of the form [DOMAIN\\]username:password.
	 */
	public WindowsCredential(String data) {
	    int ptr = data.indexOf("\\");
	    if (ptr > 0) {
		domain = data.substring(0, ptr);
		data = data.substring(ptr+1);
	    }
	    ptr = data.indexOf(":");
	    if (ptr > 0) {
		username = data.substring(0, ptr);
		password = data.substring(ptr+1).toCharArray();
	    } else {
		username = data;
	    }
	}

	public WindowsCredential(String domain, String username, char[] password) {
	    super(username, password);
	    this.domain = domain;
	}

	// Implement IWindowsCredential

	/**
	 * Return a username of the form domain\\name.
	 */
	public String getDomainUser() {
	    return new StringBuffer(domain).append('\\').append(username).toString();
	}

	public String getDomain() {
	    return domain;
	}

	public void setDomain(String domain) {
	    this.domain = domain;
	}
    }
}
