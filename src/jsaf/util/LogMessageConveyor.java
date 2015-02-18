// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;
import ch.qos.cal10n.MessageParameterObj;

/**
 * An IMessageConveyor that consolidates multiple IMessageConveyors.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class LogMessageConveyor implements IMessageConveyor {
    private HashMap<Class<? extends Enum<?>>, IMessageConveyor> conveyors;

    /**
     * Create a new conveyor.
     */
    public LogMessageConveyor() {
	conveyors = new HashMap<Class<? extends Enum<?>>, IMessageConveyor>();
    }

    /**
     * Add a conveyor to handle messages for the specified enum.
     */
    public synchronized void add(Class<? extends Enum<?>> clazz, IMessageConveyor conveyor) {
	conveyors.put(clazz, conveyor);
    }

    /**
     * Return an unmodifiable Map of enum-to-conveyor mappings handled by this instance.
     */
    public synchronized Map<Class<? extends Enum<?>>, IMessageConveyor> getConveyors() {
	return Collections.unmodifiableMap(new HashMap<Class<? extends Enum<?>>, IMessageConveyor>(conveyors));
    }

    // Implement IMessageConveyor

    public <E extends Enum<?>>String getMessage(E key, Object... args) throws MessageConveyorException {
	IMessageConveyor mc = conveyors.get(key.getDeclaringClass());
	if (mc == null) {
	    String name = key.getClass().getName();
	    throw new MessageConveyorException(name, new NoSuchElementException(name));
	} else {
	    return mc.getMessage(key, args);
	}
    }

    public String getMessage(MessageParameterObj mpo) throws MessageConveyorException {
	return getMessage(mpo.getKey(), mpo.getArgs());
    }
}
