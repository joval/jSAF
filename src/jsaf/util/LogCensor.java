// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.slf4j.cal10n.LocLogger;
import ch.qos.cal10n.MessageConveyor;

import jsaf.intf.util.ILoggable;

/**
 * An ILoggable.Censor implementation class.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class LogCensor implements ILoggable.Censor {
    static final String SANITIZED = "************";

    private Collection<CharSequence> secrets;
    private Collection<FilterLogger> filters;

    public LogCensor() {
	secrets = new ArrayList<CharSequence>();
	filters = new ArrayList<FilterLogger>();
    }

    // Implement ILoggable.Censor

    public void addKeyword(char[] keyword) {
	secrets.add((CharSequence)CharBuffer.allocate(keyword.length).put(keyword).rewind());
    }

    public void clearKeywords() {
	Iterator<CharSequence> iter = secrets.iterator();
	while(iter.hasNext()) {
	    CharSequence seq = iter.next();
	    iter.remove();
	    ((CharBuffer)seq).clear();
	}
    }

    public boolean censoring(ILoggable subject) {
	Iterator<FilterLogger> iter = filters.iterator();
	while(iter.hasNext()) {
	    if (iter.next().filtering(subject)) {
		return true;
	    }
	}
	return false;
    }

    public void add(ILoggable subject) throws IllegalStateException {
	if (censoring(subject)) {
	    throw new IllegalStateException();
	} else {
	    filters.add(new FilterLogger(this, subject));
	}
    }

    public void remove(ILoggable subject) throws IllegalStateException {
	Iterator<FilterLogger> iter = filters.iterator();
	while(iter.hasNext()) {
	    FilterLogger filter = iter.next();
	    if (filter.filtering(subject)) {
		iter.remove();
		filter.dispose();
		return;
	    }
	}
	throw new IllegalStateException();
    }

    // Internal

    Object[] expurgate(Object... args) {
	Object[] sanitized = new Object[args.length];
	Iterator<CharSequence> iter = secrets.iterator();
	while(iter.hasNext()) {
	    CharSequence secret = iter.next();
	    for (int i=0; i < args.length; i++) {
		if (args[i] instanceof String) {
		    sanitized[i] = ((String)args[i]).replace(secret, SANITIZED);
		} else {
		    sanitized[i] = args[i];
		}
	    }
	}
	return sanitized;
    }

    static class FilterLogger extends LocLogger {
	private LogCensor censor;
	private ILoggable loggable;
	private LocLogger logger;

	FilterLogger(LogCensor censor, ILoggable loggable) {
	    super(null, new MessageConveyor(Locale.getDefault()));
	    this.censor = censor;
	    this.loggable = loggable;
	    this.logger = loggable.getLogger();
	    loggable.setLogger(this);
	}

	boolean filtering(ILoggable loggable) {
	    return this.loggable == loggable;
	}

	void dispose() {
	    loggable.setLogger(logger);
	}

	// LocLogger overrides

        @Override
        public void debug(Enum<?> key, Object... args) {
	    logger.debug(key, censor.expurgate(args));
        }

        @Override
        public void error(Enum<?> key, Object... args) {
	    logger.error(key, censor.expurgate(args));
        }

        @Override
        public void info(Enum<?> key, Object... args) {
	    logger.info(key, censor.expurgate(args));
        }

        @Override
        public void trace(Enum<?> key, Object... args) {
	    logger.trace(key, censor.expurgate(args));
        }

        @Override
        public void warn(Enum<?> key, Object... args) {
	    logger.warn(key, censor.expurgate(args));
        }
    }
}
