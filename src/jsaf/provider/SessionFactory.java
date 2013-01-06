// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

import jsaf.Message;
import jsaf.intf.system.IBaseSession;

/**
 * Factory class for creating ISessions.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public abstract class SessionFactory {
    /**
     * The class name of the default factory implementation.
     */
    public static final String DEFAULT_FACTORY = "jsaf.provider.SessionFactoryImpl";

    /**
     * Obtain a new instance of a SessionFactory, with no workspace directory. 
     */
    public static SessionFactory newInstance() throws FactoryConfigurationError {
	return newInstance(null);
    }

    /**
     * Obtain a new instance of a SessionFactory, that will use the specified workspace directory. The workspace
     * directory is where sessions are permitted to save state information, which improves session performance and
     * reduces memory overhead.
     */
    public static SessionFactory newInstance(File workspace) throws FactoryConfigurationError {
	ClassLoader classLoader = SessionFactory.class.getClassLoader();
	return newInstance(DEFAULT_FACTORY, classLoader, workspace);
    }

    /**
     * Obtain a new instance of a SessionFactory from class name. This function is useful when there are multiple
     * providers in the classpath, by giving control to the application to specify which provider should be
     * loaded.
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

    // Abstract

    /**
     * Creates a session for the default target.
     */
    public abstract IBaseSession createSession() throws IOException;

    /**
     * Creates a session for the specified target. Interpretation of the target string is performed by the
     * underlying session factory implementation.
     */
    public abstract IBaseSession createSession(String target) throws IOException;
}
