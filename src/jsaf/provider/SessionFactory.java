// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.system.ISession;
import jsaf.intf.system.IRemote;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.IConnectionSpecification;

/**
 * Factory class for creating ISessions.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public abstract class SessionFactory implements ILoggable {
    /**
     * The class name of the default factory implementation.
     *
     * @since 1.0
     */
    public static final String DEFAULT_FACTORY = "jsaf.provider.LocalSessionFactory";

    /**
     * Obtain a new instance of a SessionFactory, with no workspace directory. 
     *
     * @since 1.0
     */
    public static SessionFactory newInstance() throws FactoryConfigurationError {
	return newInstance(null);
    }

    /**
     * Obtain a new instance of a SessionFactory, that will use the specified workspace directory. The workspace
     * directory is where sessions are permitted to save state information, which improves session performance and
     * reduces memory overhead.
     *
     * @since 1.0
     */
    public static SessionFactory newInstance(File workspace) throws FactoryConfigurationError {
	ClassLoader classLoader = SessionFactory.class.getClassLoader();
	return newInstance(DEFAULT_FACTORY, classLoader, workspace);
    }

    /**
     * Obtain a new instance of a SessionFactory from class name. This function is useful when there are multiple
     * providers in the classpath, by giving control to the application to specify which provider should be
     * loaded.
     *
     * @since 1.0
     */
    public static SessionFactory newInstance(String factoryClassName, ClassLoader classLoader, File workspace)
		throws FactoryConfigurationError {

	if (factoryClassName == null) {
	    throw new NullPointerException(Message.getMessage(Message.ERROR_FACTORY_CLASS));
	}
	try {
	    Class<?> clazz = classLoader.loadClass(factoryClassName);
	    Constructor initializer = null;

	    //
	    // Find the constructor that accepts a single File argument
	    //
	    for (Constructor constructor : clazz.getConstructors()) {
		Class<?>[] params = constructor.getParameterTypes();
		if (params.length == 1 && File.class == params[0]) {
		    initializer = constructor;
		    break;
		}
	    }
	    if (initializer == null) {
		String msg = Message.getMessage(Message.ERROR_FACTORY_INITIALIZER, factoryClassName);
		throw new FactoryConfigurationError(msg);
	    }
	    return (SessionFactory)initializer.newInstance(workspace);
	} catch (Exception e) {
	    throw new FactoryConfigurationError(e);
	}
    }

    protected LocLogger logger;

    protected SessionFactory() {
	logger = Message.getLogger();
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Abstract

    /**
     * Get the factory's remote session management features.
     *
     * @since 1.0
     * @deprecated
     */
    public IRemote getRemote() {
	throw new UnsupportedOperationException();
    }

    /**
     * Creates a session for the default target.
     *
     * @since 1.0
     */
    public abstract ISession createSession() throws IOException;

    /**
     * Creates a session for the specified target. Interpretation of the target string is performed by the
     * underlying session factory implementation.
     *
     * @since 1.0
     */
    public abstract ISession createSession(String target) throws IOException;

    /**
     * Creates a session for the specified target.
     *
     * @since 1.0.2
     */
    public abstract ISession createSession(IConnectionSpecification target) throws IOException;
}
