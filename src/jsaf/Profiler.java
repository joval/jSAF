// Copyright (C) 2013 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.cal10n.LocLogger;

/**
 * A utility class for profiling the total elapsed time taken to perform keyed operations. To use:
 * <pre>
 * Profiler profiler = new Profiler();
 * Integer id = profiler.begin("stuff");
 * // ... do a bunch of stuff
 * profiler.end(id);
 * </pre>
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Profiler {
    private Map<String, Collection<Invocation>> invocations;
    private Map<Integer, Timer> timers;
    private int counter;

    public Profiler() {
	invocations = new HashMap<String, Collection<Invocation>>();
	timers = new HashMap<Integer, Timer>();
	counter = 0;
    }

    /**
     * Start a timer for the key, and return its ID.
     */
    public Integer begin(String key) {
	StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
	String caller = ste.getClassName() + "." + ste.getMethodName() + " line " + ste.getLineNumber();
	Integer id = new Integer(counter++);
	timers.put(id, new Timer(caller, key, System.currentTimeMillis()));
	return id;
    }

    /**
     * Add the transpired interval since the start of the timer with the specified ID to its corresponding key.
     */
    public void end(Integer timerId) {
	long end = System.currentTimeMillis();
	Timer timer = timers.remove(timerId);
	long duration = end - timer.started();
	if (!invocations.containsKey(timer.key())) {
	    invocations.put(timer.key(), new ArrayList<Invocation>());
	}
	invocations.get(timer.key()).add(new Invocation(timer.caller(), duration));
    }

    /**
     * Return all the keys for which there is elapsed time data, sorted alphanumerically.
     */
    public String[] keys() {
	String[] sorted = invocations.keySet().toArray(new String[invocations.size()]);
	Arrays.sort(sorted);
	return sorted;
    }

    /**
     * Return the elapsed time for the specified key.
     */
    public long elapsed(String key) {
	long elapsed = 0L;
	if (invocations.containsKey(key)) {
	    for (Invocation invocation : invocations.get(key)) {
		elapsed += invocation.time;
	    }
	}
	return elapsed;
    }

    public void print(PrintStream out) {
	out.println("---------------------------------------------------------------");
	out.println("PROFILE DATA:");
	for (String key : keys()) {
	    out.println("  " + key + ": " + invocations.get(key).size() + " invocations, total: " + elapsed(key) + "ms");
	    for (Invocation invocation : invocations.get(key)) {
		out.println("    " + invocation.caller + ": " + invocation.time + "ms");
	    }
	}
	out.println("---------------------------------------------------------------");
    }

    public void trace(LocLogger logger) {
	logger.trace("---------------------------------------------------------------");
	logger.trace("PROFILE DATA:");
	for (String key : keys()) {
	    logger.trace("  " + key + ": " + invocations.get(key).size() + " invocations, total: " + elapsed(key) + "ms");
	    for (Invocation invocation : invocations.get(key)) {
		logger.trace("    " + invocation.caller + ": " + invocation.time + "ms");
	    }
	}
	logger.trace("---------------------------------------------------------------");
    }

    // Internal

    class Timer {
	private String caller, key;
	private long time;

	Timer(String caller, String key, long time) {
	    this.caller = caller;
	    this.key = key;
	    this.time = time;
	}

	String caller() {
	    return caller;
	}

	String key() {
	    return key;
	}

	long started() {
	    return time;
	}
    }

    class Invocation {
	String caller;
	long time;

	Invocation(String caller, long time) {
	    this.caller = caller;
	    this.time = time;
	}
    }
}
