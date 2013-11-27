// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.wmi;

import java.util.Hashtable;

import org.slf4j.cal10n.LocLogger;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import jsaf.Message;
import jsaf.intf.util.ILoggable;
import jsaf.intf.windows.wmi.ISWbemEventSource;
import jsaf.intf.windows.wmi.ISWbemObjectSet;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.provider.windows.wmi.WmiException;
import jsaf.provider.windows.wmi.scripting.SWbemObjectSet;

/**
 * A simple class for performing WMI queries.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class WmiProvider implements IWmiProvider {
    private ActiveXComponent locator;
    private Hashtable <String, Dispatch>map;
    private LocLogger logger;
    private long timeout;

    public WmiProvider(ILoggable log, long timeout) {
	logger = log.getLogger();
	this.timeout = timeout;
	map = new Hashtable<String, Dispatch>();
    }

    public boolean register() {
	try {
	    if (locator == null) {
		logger.debug(Message.STATUS_WMI_CONNECT);
		locator = new ActiveXComponent("WbemScripting.SWbemLocator");
	    }
	    return true;
	} catch (UnsatisfiedLinkError e) {
	    logger.error(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    return false;
	}
    }

    public void deregister() {
	if (locator != null) {
	    logger.debug(Message.STATUS_WMI_DISCONNECT);
	    locator.safeRelease();
	    locator = null;
	}
	map.clear();
    }

    // Implement ILoggable

    public LocLogger getLogger() {
	return logger;
    }

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    // Implement ISWbemProvider

    public ISWbemObjectSet execQuery(String ns, String wql) throws WmiException {
	logger.debug(Message.STATUS_WMI_QUERY, ns, wql);
	Dispatch services = map.get(ns);
	if (services == null) {
	    services = locator.invoke("ConnectServer", Variant.DEFAULT, new Variant(ns)).toDispatch();
	    map.put(ns, services);
	}
	QueryRunner runner = new QueryRunner(services, wql);
	if (timeout > 0) {
	    runner.start();
	    try {
		runner.join(timeout);
		if (runner.isAlive()) {
		    runner.interrupt();
		    logger.warn(Message.ERROR_WMI_TIMEOUT, wql);
		}
	    } catch (InterruptedException e) {
		throw new WmiException(e);
	    }
	} else {
	    runner.run();
	}
	return runner.result();
    }

    public ISWbemEventSource execNotificationQuery(String ns, String wql) throws WmiException {
	throw new WmiException("unsupported");
    }

    // Private

    class QueryRunner implements Runnable {
	private Dispatch services;
	private String wql;
	private Thread thread;
	private SWbemObjectSet result;
	private WmiException error;

	QueryRunner(Dispatch services, String wql) {
	    this.services = services;
	    this.wql = wql;
	}

	void start() {
	    thread = new Thread(this);
	    thread.start();
	}

	void join(long millis) throws InterruptedException {
	    thread.join(millis);
	}

	boolean isAlive() {
	    return thread.isAlive();
	}

	void interrupt() {
	    if (error == null) {
		error = new WmiException(Message.getMessage(Message.ERROR_WMI_TIMEOUT, wql));
	    }
	    thread.interrupt();
	}

	ISWbemObjectSet result() throws WmiException {
	    if (error == null) {
		return result;
	    } else {
		throw error;
	    }
	}

	// Implement Runnable

	public void run() {
	    try {
		result = new SWbemObjectSet(Dispatch.call(services, "ExecQuery", wql).toDispatch());
	    } catch (Exception e) {
		error = new WmiException(e);
	    }
	}
    }
}
