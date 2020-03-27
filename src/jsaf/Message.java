// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;
import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;
import ch.qos.cal10n.MessageParameterObj;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import jsaf.util.LogMessageConveyor;

/**
 * Uses cal10n to define localized messages for jSAF.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
@BaseName("jsafmsg")
@LocaleData(
  defaultCharset="ASCII",
  value = { @Locale("en_US") }
)
public enum Message {
    ERROR_EOF,
    ERROR_EOS,
    ERROR_EXCEPTION,
    ERROR_FACTORY_CLASS,
    ERROR_FACTORY_INITIALIZER,
    ERROR_IO,
    ERROR_IO_NOT_DIR,
    ERROR_MEMORY_URL_MAPPING,
    ERROR_PROCESS_RETRY,
    ERROR_PROTOCOL,
    ERROR_SEARCH_CONDITION,
    ERROR_SEARCH_FIELD,
    ERROR_SESSION_INTEGRITY,
    ERROR_SID,
    ERROR_TFTP,
    ERROR_TRUNCATE,
    ERROR_UNIX_FLAVOR,
    STATUS_COMMAND_OUTPUT_PROGRESS,
    STATUS_COMMAND_OUTPUT_TEMP,
    STATUS_PROCESS_RETRY,
    STATUS_REGEX_GLOB,
    STATUS_REGEX_GLOB_FAILED,
    STATUS_REGEX_GUESS,
    STATUS_REGEX_GUESS_ALTERNATION,
    STATUS_REGEX_GUESS_COUNT,
    STATUS_REGEX_GUESS_OVERFLOW,
    WARNING_COMMAND_OUTPUT,
    WARNING_MISSING_OUTPUT,
    WARNING_PERISHABLEIO_INTERRUPT,
    WARNING_READER_THREAD,
    WARNING_UNSAFE_CHARS,
    WARNING_UNIX_FLAVOR;

    private static IMessageConveyor baseConveyor;
    private static LogMessageConveyor conveyor;
    private static LocLoggerFactory loggerFactory;
    private static LocLogger sysLogger;

    static {
	baseConveyor = new MessageConveyor(java.util.Locale.getDefault());
	try {
	    //
	    // Get a message to test whether localized messages are available for the default Locale
	    //
	    baseConveyor.getMessage(ERROR_EXCEPTION);
	} catch (MessageConveyorException e) {
	    //
	    // The test failed, so set the message Locale to English
	    //
	    baseConveyor = new MessageConveyor(java.util.Locale.ENGLISH);
	}
	conveyor = new LogMessageConveyor();
	conveyor.add(Message.class, baseConveyor);
	loggerFactory = new LocLoggerFactory(conveyor);
	sysLogger = loggerFactory.getLocLogger(Message.class);
    }

    /**
     * Extend Message to be able to provide messages for the specified Enum class, using the specified IMessageConveyor.
     */
    public static void extend(Class<? extends Enum<?>> clazz, IMessageConveyor mc) {
	conveyor.add(clazz, mc);
    }

    /**
     * Retrieve the default localized system logger used by the jSAF library.
     */
    public static LocLogger getLogger() {
	return sysLogger;
    }

    /**
     * Retrieve the default localized system logger used by the jSAF library.
     *
     * @since 1.3
     */
    public static LocLogger getLogger(String name) {
	return loggerFactory.getLocLogger(name);
    }

    /**
     * Retrieve a localized String, given the key and substitution arguments.
     */
    public static String getMessage(Enum<?> key, Object... args) {
	return conveyor.getMessage(key, args);
    }

    public static Set<Map.Entry<Class<? extends Enum<?>>, IMessageConveyor>> getConveyors() {
	return conveyor.getConveyors().entrySet();
    }
}
