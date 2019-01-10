// Copyright (C) 2011-2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;
import java.lang.reflect.Constructor;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.remote.ConnectionEvent;
import jsaf.intf.remote.IConnectionSpecification;
import jsaf.intf.remote.IConnectionSpecificationFactory;
import jsaf.intf.system.ISession;
import jsaf.intf.util.ILoggable;
import jsaf.util.Publisher;

/**
 * Factory class for creating ISessions. To be loaded using this class's convenience methods, subclasses must implement a
 * single-argument constructor that accepts a File indicating a workspace directory for caching temporary files.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public abstract class SessionFactory extends Publisher<ConnectionEvent> implements ILoggable {
    /**
     * The class name of the default (local) factory implementation.
     *
     * @since 1.0
     */
    public static final String DEFAULT_FACTORY = "jsaf.provider.LocalSessionFactory";

    /**
     * The class name of the default remote factory implementation.
     *
     * @since 1.2
     */
    public static final String REMOTE_FACTORY = "jsaf.provider.RemoteSessionFactory";

    /**
     * The class name of the default offline factory implementation.
     *
     * @since 1.2
     */
    public static final String OFFLINE_FACTORY = "jsaf.provider.OfflineSessionFactory";

    /**
     * The class name of the default agent-based factory implementation.
     *
     * @since 1.3.7
     */
    public static final String AGENT_FACTORY = "jsaf.provider.AgentSessionFactory";

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
	return newInstance(DEFAULT_FACTORY, null, workspace);
    }

    /**
     * Obtain a new instance of a SessionFactory from class name. This function is useful when there are multiple
     * providers in the classpath, by giving control to the application to specify which provider should be
     * loaded.
     *
     * @param factoryClassName The class name of the desired factory implementation class.
     *
     * @param classLoader      The ClassLoader from which to load the factory. If null is specified, this will be the
     *                         ClassLoader for the SessionFactory class.
     *
     * @param workspace        A directory in which jSAF sessions can create cache files. If null is specified, no caches
     *                         will be created.
     *
     * @since 1.0
     */
    public static SessionFactory newInstance(String factoryClassName, ClassLoader classLoader, File workspace) throws FactoryConfigurationError {
	if (factoryClassName == null) {
	    throw new NullPointerException(Message.getMessage(Message.ERROR_FACTORY_CLASS));
	}
	if (classLoader == null) {
	    classLoader = SessionFactory.class.getClassLoader();
	}
	try {
	    Class<?> clazz = classLoader.loadClass(factoryClassName);
	    Constructor initializer = null;

	    //
	    // Find the constructor that accepts a single File argument
	    //
	    for (Constructor constructor : clazz.getDeclaredConstructors()) {
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
	    initializer.setAccessible(true);
	    return (SessionFactory)initializer.newInstance(workspace);
	} catch (Exception e) {
	    throw new FactoryConfigurationError(e);
	}
    }

    protected LocLogger logger;

    protected SessionFactory() {
	logger = Message.getLogger();
	start();
    }

    @Override
    protected void finalize() {
	stop();
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
     * Creates a session for the default target.
     *
     * @since 1.0
     */
    public abstract ISession createSession() throws SessionException;

    /**
     * Creates a session for the specified target. Interpretation of the target string is performed by the
     * underlying session factory implementation.
     *
     * @since 1.1
     */
    public abstract ISession createSession(IConnectionSpecification spec) throws SessionException;
}
