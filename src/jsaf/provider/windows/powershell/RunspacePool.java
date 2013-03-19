// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.powershell;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.HashMap;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.system.IProcess;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.intf.windows.powershell.IRunspacePool;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.util.StringTools;

/**
 * An implementation of a runspace pool based on a generic IWindowsSession.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class RunspacePool implements IRunspacePool {
    /**
     * Until Java 1.7, a process locks up when its output buffer fills up.
     */
    static final boolean PRE_17 = new Float("1.7").compareTo(new Float(System.getProperty("java.specification.version"))) > 0;

    private LocLogger logger;
    private IWindowsSession session;
    private Charset encoding;
    private boolean buffered;
    private HashMap<String, Runspace> pool;
    private int capacity;

    public RunspacePool(IWindowsSession session, int capacity) {
	this(session, capacity, StringTools.ASCII, !PRE_17);
    }

    public RunspacePool(IWindowsSession session, int capacity, Charset encoding, boolean buffered) {
	this.session = session;
	logger = session.getLogger();
	this.capacity = capacity;
	this.encoding = encoding;
	this.buffered = buffered;
	pool = new HashMap<String, Runspace>();
    }

    public void shutdown() {
	for (Runspace runspace : pool.values()) {
	    try {
		runspace.invoke("exit", 2000L);
		IProcess p = runspace.getProcess();
		if (p.isRunning()) {
		    p.destroy();
		}
		logger.debug(Message.STATUS_POWERSHELL_EXIT, runspace.getId());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	pool.clear();
    }

    @Override
    protected void finalize() {
	shutdown();
    }

    // Implement IRunspacePool

    public synchronized Collection<IRunspace> enumerate() {
	Collection<IRunspace> runspaces = new ArrayList<IRunspace>();
	for (Runspace rs : pool.values()) {
	    runspaces.add(rs);
	}
	return runspaces;
    }

    public int capacity() {
	return capacity;
    }

    public IRunspace get(String id) throws NoSuchElementException {
	if (pool.containsKey(id)) {
	    return pool.get(id);
	} else {
	    throw new NoSuchElementException(id);
	}
    }

    public synchronized IRunspace spawn() throws Exception {
	return spawn(session.getNativeView());
    }

    public synchronized IRunspace spawn(IWindowsSession.View view) throws Exception {
	if (pool.size() < capacity()) {
	    String id = Integer.toString(pool.size());
	    Runspace runspace = new Runspace(id, session, view, encoding, buffered);
	    logger.debug(Message.STATUS_POWERSHELL_SPAWN, id);
	    pool.put(id, runspace);
	    runspace.invoke("$host.UI.RawUI.BufferSize = New-Object System.Management.Automation.Host.Size(512,2000)");
	    runspace.loadModule(RunspacePool.class.getResourceAsStream("Transfer.psm1"));
	    return runspace;
	} else {
	    throw new IndexOutOfBoundsException(Integer.toString(pool.size() + 1));
	}
    }
}
