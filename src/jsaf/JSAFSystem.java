// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jsaf.protocol.JSAFURLStreamHandlerFactory;

/**
 * This class is used to retrieve JSAF-wide resources, like the location of the JSAF workspace directory, and the
 * JSAF event system timer.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public final class JSAFSystem {
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;

    private static Timer timer;
    private static Map<Runnable, TimerTask> tasks = new HashMap<Runnable, TimerTask>();
    private static File dataDir = null;
    private static boolean registeredHandlers = false;

    static {
	if (WINDOWS) {
	    String s = System.getenv("LOCALAPPDATA");
	    if (s == null) {
		s = System.getenv("APPDATA");
	    }
	    if (s != null && s.toLowerCase().indexOf("system32") != -1) {
		s = System.getenv("ProgramData");
	    }
	    if (s != null) {
		File appDataDir = new File(s);
		dataDir = new File(appDataDir, "jSAF");
	    }
	}
	if (dataDir == null) {
	    File homeDir = new File(System.getProperty("user.home"));
	    dataDir = new File(homeDir, ".jSAF");
	}
	timer = new JSAFSystemTimer();
    }

    /**
     * Register the JSAF URL handlers for the tftp, zip and memory protocols.
     */
    public static void registerURLHandlers() {
	if (!registeredHandlers) {
	    URL.setURLStreamHandlerFactory(JSAFURLStreamHandlerFactory.getInstance());
	    registeredHandlers = true;
	}
    }

    /**
     * Retrieve the daemon Timer used for scheduled jSAF tasks.
     *
     * @deprecated since 1.4. Use the schedule methods instead.
     */
    @Deprecated
    public static Timer getTimer() {
	return timer;
    }

    /**
     * Schedules the specified task for execution at the specified time.
     *
     * @since 1.4
     */
    public static synchronized void schedule(Runnable task, Date time) {
	Task wrapper = new Task(task);
	timer.schedule(wrapper, time);
	tasks.put(task, wrapper);
    }

    /**
     * Schedules the specified task for repeated fixed-delay execution, beginning at the specified time.
     *
     * @since 1.4
     */
    public static synchronized void schedule(Runnable task, Date firstTime, long period) {
	Task wrapper = new Task(task);
	timer.schedule(wrapper, firstTime, period);
	tasks.put(task, wrapper);
    }

    /**
     * Schedules the specified task for execution after the specified delay.
     *
     * @since 1.4
     */
    public static synchronized void schedule(Runnable task, long delay) {
	Task wrapper = new Task(task);
	timer.schedule(wrapper, delay);
	tasks.put(task, wrapper);
    }

    /**
     * Schedules the specified task for repeated fixed-delay execution, beginning after the specified delay.
     *
     * @since 1.4
     */
    public static synchronized void schedule(Runnable task, long delay, long period) {
	Task wrapper = new Task(task);
	timer.schedule(wrapper, delay, period);
	tasks.put(task, wrapper);
    }

    /**
     * Cancel a scheduled task.
     *
     * @return true if this task is scheduled for one-time execution and has not yet run, or this task is scheduled
     *         for repeated execution. Returns false if the task was scheduled for one-time execution and has already
     *         run, or if the task was never scheduled, or if the task was already cancelled. (Loosely speaking, this
     *         method returns true if it prevents one or more scheduled executions from taking place.)
     *
     * @since 1.4
     */
    public static synchronized boolean cancelTask(Runnable task) {
	if (tasks.containsKey(task)) {
	    return tasks.remove(task).cancel();
	} else {
	    return false;
	}
    }

    public static void setDataDirectory(File dir) throws IllegalArgumentException {
	if (dir.isDirectory()) {
	    dataDir = dir;
	} else {
	    String reason = Message.getMessage(Message.ERROR_IO_NOT_DIR);
	    throw new IllegalArgumentException(Message.getMessage(Message.ERROR_IO, dir.toString(), reason));
	}
    }

    /**
     * Return a directory suitable for storing transient application data, like state information that may persist
     * between invocations.  This is either a directory called .jSAF beneath the user's home directory, or on Windows,
     * it will be a directory named jSAF in the appropriate AppData storage location.
     */
    public static File getDataDirectory() {
	if (!dataDir.exists()) {
	    dataDir.mkdirs();
	}
	return dataDir;
    }

    // Private

    static class Task extends TimerTask {
	private Runnable task;

	Task(Runnable task) {
	    this.task = task;
	}

	public void run() {
	    try {
		task.run();
	    } catch (Throwable t) {
		Message.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), t);
	    }
	}

	@Override
	public boolean cancel() {
	    boolean result = super.cancel();
	    JSAFSystem.timer.purge();
	    return result;
	}
    }

    static class JSAFSystemTimer extends Timer {
	JSAFSystemTimer() {
	    super("jSAF System Timer", true);
	}

	@Override
	public void cancel() {
	    // cannot be cancelled
	}
    }
}
