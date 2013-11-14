// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.system.ISession;
import jsaf.intf.util.IProperty;
import jsaf.intf.util.IConfigurable;
import jsaf.util.PropertyUtil;

/**
 * This is the base class for ALL the implementations of all the different types of jSAF sessions. A session has very
 * limited capabilities -- properties, a hostname, a username, and the concept of a "connection". Extended interfaces
 * expose more in-depth capabilities of (potentially) both physical and logical endpoints.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public abstract class BaseSession implements IConfigurable, ISession {
    protected File wsdir = null;
    protected LocLogger logger;
    protected InternalProperties internalProps;
    protected boolean connected = false;

    protected BaseSession() {
	logger = Message.getLogger();
	internalProps = new InternalProperties();
	Configurator.configure(this);
    }

    /**
     * Subclasses may override this method in order to define an "override key" for any property key whose value, if it
     * exists, should override the value associated with the original property key.
     */
    protected String getOverrideKey(String key) {
        return null;
    }

    /**
     * Subclasses may override this method if changes made to the IProperty should cause some immediate effect to occur
     * on the operation of the subclass.
     */
    protected void handlePropertyChange(String key, String value) {}

    // Implement ILoggable

    public LocLogger getLogger() {
	return logger;
    }

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    // Implement IConfigurable

    public IProperty getProperties() {
	return internalProps;
    }

    // Implement ISession

    public String getHostname() {
	return LOCALHOST;
    }

    public String getUsername() {
	return null;
    }

    public long getTimeout(Timeout to) {
	switch(to) {
	  case M:
	    return internalProps.getLongProperty(PROP_READ_TIMEOUT_M);

	  case L:
	    return internalProps.getLongProperty(PROP_READ_TIMEOUT_L);

	  case XL:
	    return internalProps.getLongProperty(PROP_READ_TIMEOUT_XL);

	  case S:
	  default:
	    return internalProps.getLongProperty(PROP_READ_TIMEOUT_S);
	}
    }

    public File getWorkspace() {
	return wsdir;
    }

    public boolean connect() {
	return connected = true;
    }

    public boolean isConnected() {
	return connected;
    }

    public void disconnect() {
	connected = false;
    }

    public void dispose() {
	logger.info(Message.STATUS_SESSION_DISPOSE, getHostname());
    }

    /**
     * Extended ISession types (i.e., not defined in the ISession.Type enumeration) should override this method.
     */
    public String getTypeVal() {
	return getType().value();
    }

    // Private

    protected class InternalProperties implements IProperty {
	private PropertyUtil props;

	protected InternalProperties() {
	    props = new PropertyUtil();
	}

	// Implement IProperty

	public void setProperty(String key, String value) {
	    props.setProperty(key, value);
	    handlePropertyChange(key, value);
	}

	public boolean containsKey(String key) {
	    return getProperty(key) != null;
	}

	/**
	 * First checks for a property with the override key, then returns the requested key if none exists.
	 */
	public String getProperty(String key) {
	    String ok = getOverrideKey(key);
	    if (ok != null) {
		String val =  props.getProperty(ok);
		if (val != null) {
		    return val;
		}
	    }
	    return props.getProperty(key);
	}

	public long getLongProperty(String key) {
	    long l = 0L;
	    try {
		String val = getProperty(key);
		if (val != null) {
		    l = Long.parseLong(val);
		}
	    } catch (NumberFormatException e) {
	    }
	    return l;
	}

	public int getIntProperty(String key) {
	    int i = 0;
	    try {
		String val = getProperty(key);
		if (val != null) {
		    i = Integer.parseInt(val);
		}
	    } catch (NumberFormatException e) {
	    }
	    return i;
	}

	public boolean getBooleanProperty(String key) {
	    return "true".equalsIgnoreCase(getProperty(key));
	}

	public Iterator<String> iterator() {
	    return props.iterator();
	}

	public Properties toProperties() {
	    return props.toProperties();
	}
    }
}
