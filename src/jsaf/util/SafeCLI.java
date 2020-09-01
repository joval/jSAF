// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.cal10n.LocLogger;

import jsaf.JSAFSystem;
import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.io.IReader;
import jsaf.intf.system.IProcess;
import jsaf.intf.system.IComputerSystem;
import jsaf.intf.system.ISession.Timeout;
import jsaf.intf.util.IProperty;
import jsaf.io.LineIterator;
import jsaf.io.PerishableReader;
import jsaf.io.SimpleReader;
import jsaf.io.Streams;
import jsaf.io.TruncatedInputStream;
import jsaf.provider.SessionException;

import static jsaf.intf.system.ISession.LOCALHOST;

/**
 * A utility that simplifyies the execution of simple command-lines.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class SafeCLI {
    /**
     * ANSI C locale environment.
     *
     * @since 1.3.10
     */
    public static final String[] ANSI_C = new String[] {"LC_ALL=C"};

    /**
     * en_US UTF8 locale environment.
     *
     * @since 1.3.10
     */
    public static final String[] en_US = new String[] {"LC_ALL=en_US.UTF-8"};

    /**
     * ISession property key for specifying whether the manyLines methods should redirect to a temp file (true),
     * or operate on the live process output (false).
     *
     * @since 1.5.0
     */
    public static final String REDIRECT_PROP = "SafeCLI.redirect";

    /**
     * ISession property key for specifying the gzip executable path for temp file compression.
     *
     * @since 1.5.0
     */
    public static final String GZIP_PROP = "SafeCLI.gzip";

    /**
     * An interface for processing data from a process stream (stdout or stderr), used by the exec method.
     *
     * If the command hangs during processing (signaled to SafeCLI by an InterruptedIOException or SessionException),
     * the SafeCLI will kill the old command, start a new command instance (up the the session's configured number of
     * retries), and call handler.handle again.
     *
     * Therefore, the handler should initialize itself completely when handle is invoked, and not perform permanent
     * output processing until the reader has reached the end of the process output.
     *
     * @since 1.3
     */
    public interface IReaderHandler {
	/**
	 * Handle data from the reader. Implementations must NEVER catch an IOException originating from the reader!
	 *
	 * NOTE: This method will be called multiple times if the process must retry, so be sure to perform (re-)initialization
	 *       within the implementation of this method.
	 *
	 * @since 1.3
	 */
	public void handle(IReader reader) throws IOException;
    }

    /**
     * An IReaderHandler that does nothing with data read from the IReader.
     *
     * @since 1.3
     */
    public static final IReaderHandler DevNull = new IReaderHandler() {
	public void handle(IReader reader) throws IOException {
	    String line;
	    while ((line = reader.readLine()) != null) {
	    }
	}
    };

    /**
     * On occasion, it is necessary to incorporate a String that originates from an un-trusted source into a command-line
     * statement.  This method verifies that the untrusted string cannot have any unintended side-effects by injecting
     * additional commands into the statement (potentially maliciously). Primarily, this is accomplished by insuring that
     * the string contains no quotes, so that it cannot terminate any enclosing quotes thereby obtaining access to the shell.
     *
     * @return the input String (if no exception is thrown)
     *
     * @throws IllegalArgumentException if a potentially-malicious pattern has been detected.
     *
     * @since 1.0
     */
    public static String checkArgument(String arg, IComputerSystem sys) throws IllegalArgumentException {
	char[] chars = arg.toCharArray();
	switch(sys.getType()) {
	  case WINDOWS:
	    for (int i=0; i < chars.length; i++) {
		//
		// Powershell automatically converts 'smart-quotes' into regular quotes, so we have to specifically
		// check for them.
		//
		switch((int)chars[i]) {
	  	  case 0x22:	// ASCII double quote
		  case 0x27:	// ASCII single quote
		  case 0x2018:	// Unicode single left quote
		  case 0x2019:	// Unicode single right quote
		  case 0x201a:	// Unicode single low quote
		  case 0x201c:	// Unicode double left quote
		  case 0x201d:	// Unicode double right quote
		  case 0x201e:	// Unicode double low quote
		    throw new IllegalArgumentException(Message.getMessage(Message.WARNING_UNSAFE_CHARS, arg));
		  default:
		    break;
		}
	    }
	    break;

	  case UNIX:
	    for (int i=0; i < chars.length; i++) {
		switch((int)chars[i]) {
		  case 0x22:	// ASCII double quote
		  case 0x27:	// ASCII single quote
		  case 0x60:	// ASCII back-tick
		    throw new IllegalArgumentException(Message.getMessage(Message.WARNING_UNSAFE_CHARS, arg));
		  default:
		    break;
		}
	    }
	    break;
	}
	return arg;
    }

    /**
     * Run a command in the local environment without using a session.
     *
     * @since 1.5.0
     */
    public static final String exec(String cmd, long timeout, LocLogger logger) throws IOException {
	logger.debug("Exec: " + cmd);
	List<String> argv = null; 
	if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) { 
	    argv = Arrays.<String>asList(new String[] {"/bin/sh", "-c", cmd});
	} else {
	    argv = Arrays.<String>asList(new String[] {"cmd", "/c", cmd});
	}
	ProcessBuilder pb = new ProcessBuilder(argv);
	pb.redirectErrorStream(true);
	InputStream in = null;
	try {
	    Process p = pb.start();
	    in = PerishableReader.newInstance(p.getInputStream(), timeout);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buff = new byte[1024];
	    int len = 0;
	    while ((len = in.read(buff)) > 0) {
		out.write(buff, 0, len);
	    }
	    String output = new String(out.toByteArray(), Strings.UTF8);
	    logger.trace("Output: " + output);
	    return output;
	} finally {
	    if (in != null) {
		try {
		    in.close();
		} catch (IOException e) {
		}
	    }
	}
    }

    /**
     * Run a command in the local environment without using a session.
     *
     * @since 1.5.1
     */
    public static final List<String> multiLine(String cmd, long timeout, LocLogger logger) throws IOException {
	return Strings.toList(new LineIterator(new ByteArrayInputStream(exec(cmd, timeout, logger).getBytes(Strings.UTF8))));
    }

    /**
     * Run a command and get the first (non-empty) line of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, IComputerSystem sys, Timeout readTimeout) throws IOException {
	return exec(cmd, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, IComputerSystem sys, Timeout readTimeout) throws IOException {
	return exec(cmd, env, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, String dir, IComputerSystem sys, Timeout readTimeout) throws IOException {
	return exec(cmd, env, dir, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the first (non-empty) line of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, IComputerSystem sys, long readTimeout) throws IOException {
	return exec(cmd, null, null, sys, readTimeout);
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, IComputerSystem sys, long readTimeout) throws IOException {
	return exec(cmd, env, null, sys, readTimeout);
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws IOException {
	List<String> lines = multiLine(cmd, env, dir, sys, readTimeout);
	if (lines.size() == 2 && lines.get(0).equals("")) {
	    return lines.get(1);
	} else {
	    return lines.get(0);
	}
    }

    /**
     * Run a command and get the resulting lines of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, IComputerSystem sys, Timeout readTimeout) throws IOException {
	return multiLine(cmd, null, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, IComputerSystem sys, Timeout readTimeout) throws IOException {
	return multiLine(cmd, env, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, String dir, IComputerSystem sys, Timeout readTimeout) throws IOException {
	return multiLine(cmd, env, dir, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the resulting lines of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, IComputerSystem sys, long readTimeout) throws IOException {
	return multiLine(cmd, null, null, sys, readTimeout);
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, IComputerSystem sys, long readTimeout) throws IOException {
	return multiLine(cmd, env, null, sys, readTimeout);
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws IOException {
	return execData(cmd, env, dir, sys, readTimeout).getLines();
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment. This command assumes that
     * there will be a large volume of output from the command, so it will redirect the output to a file, transfer the file
     * locally (if sys is not a local session), and then return an iterator that reads lines from the local file.
     *
     * When the end of the iterator is reached, the local file is deleted.
     *
     * @since 1.0.1
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IComputerSystem sys) throws IOException {
	return manyLines(cmd, env, new ErrorLogger(sys), sys, sys.getTimeout(Timeout.XL));
    }

    /**
     * Pass in a custom timeout to the manyLines command.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IComputerSystem sys, Timeout timeout) throws IOException {
	return manyLines(cmd, env, sys, sys.getTimeout(timeout));
    }

    /**
     * Pass in a custom timeout to the manyLines command.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IComputerSystem sys, long timeout) throws IOException {
	return manyLines(cmd, env, new ErrorLogger(sys), sys, timeout);
    }

    /**
     * Pass in a custom error stream handler to the manyLines command.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IReaderHandler errHandler, IComputerSystem sys, Timeout timeout) throws IOException {
	return manyLines(cmd, env, errHandler, sys, sys.getTimeout(timeout));
    }

    static final String OPEN = "(";
    static final String CLOSE = ")";

    /**
     * Pass in a custom error stream handler to the manyLines command, with a custom timeout.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IReaderHandler errHandler, IComputerSystem sys, long timeout) throws IOException {
	IProperty props = sys.getProperties();
	for (int attempt=1; true; attempt++) {
	    if (!sys.isConnected()) {
		sys.connect();
	    }
	    if (props.getBooleanProperty(REDIRECT_PROP)) {
		//
		// Modify the command to redirect output to a temp file (compressed), and periodically check the size of the file
		//
		FileMonitor mon = new FileMonitor(sys.getFilesystem());
		JSAFSystem.schedule(mon, 15000, 15000);
		try {
		    IFile remoteTemp = sys.getFilesystem().createTempFile("cmd", ".out", null);
		    String tempPath = remoteTemp.getPath();
		    mon.setPath(tempPath);
		    String redirected;
		    if ((cmd.indexOf(";") != -1 || cmd.indexOf("&&") != -1) && !cmd.startsWith(OPEN) && !cmd.endsWith(CLOSE)) {
			//
			// Multiple comands have to be grouped, or only the last one's output will be redirected.
			//
			redirected = new StringBuffer(OPEN).append(cmd).append(CLOSE).toString();
		    } else {
			redirected = cmd;
		    }
		    if (props.containsKey(GZIP_PROP)) {
			redirected = new StringBuffer(redirected).append(" | ").append(props.getProperty(GZIP_PROP)).append(" > ").append(tempPath).toString();
		    } else {
			redirected = new StringBuffer(redirected).append(" > ").append(tempPath).toString();
		    }

		    SafeCLI cli = new SafeCLI(redirected, env, null, sys, timeout);
		    if (cli.execOnce(null, errHandler, attempt)) {
			//
			// Create and return a LineIterator based on a local cache file containing the output
			//
			if (LOCALHOST.equals(sys.getHostname())) {
			    //
			    // output was redirected to a local file that we can use directly as the cache
			    //
			    return new LineIterator(new File(tempPath));
			} else {
			    //
			    // output is potentially in a remote file, so we must copy its contents to a local cache
			    //
			    File tempDir = sys.getWorkspace() == null ? new File(System.getProperty("user.home")) : sys.getWorkspace();
			    File localTemp = File.createTempFile("cmd", null, tempDir);
			    try {
				Streams.copy(remoteTemp.getInputStream(), new FileOutputStream(localTemp), true);
				remoteTemp.delete();
				return new LineIterator(localTemp);
			    } catch (IOException e) {
				if (attempt > cli.execRetries) {
				    throw e;
				} else {
				    sys.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
				}
			    }
			}
		    }
		} finally {
		    JSAFSystem.cancelTask(mon);
		}
	    } else {
		//
		// Iterate over the live stdout, while handling stderr in another thread
		//
		IProcess p = sys.createProcess(cmd, env, null);
		p.start();
		new HandlerThread(errHandler, "stderr reader", sys.getLogger()).start(new SimpleReader(p.getErrorStream()));
		return new OutputLineIterator(p, timeout);
	    }
	}
    }

    /**
     * Run a command and get the resulting ExecData, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final ExecData execData(String cmd, String[] env, IComputerSystem sys, long readTimeout) throws IOException {
	return execData(cmd, env, null, sys, readTimeout);
    }

    /**
     * Run a command and get the resulting ExecData, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final ExecData execData(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws IOException {
	SafeCLI cli = new SafeCLI(cmd, env, dir, sys, readTimeout);
	cli.exec();
	return cli.getResult();
    }

    /**
     * Run a command, using the specified output and error handlers.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @see IReaderHandler
     * @since 1.3
     */
    public static final void exec(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout, IReaderHandler out, IReaderHandler err)
		throws IOException {

	new SafeCLI(cmd, env, dir, sys, readTimeout).exec(out, err);
    }

    /**
     * A container for information resulting from the execution of a process.
     *
     * @since 1.0
     */
    public static class ExecData {
	private String cmd;
	private LocLogger logger;
	private int exitCode;
	private byte[] data, err;

	ExecData(String cmd, LocLogger logger) {
	    this.cmd = cmd;
	    this.logger = logger;
	    exitCode = -1;
	    data = null;
	    err = null;
	}

	/**
	 * Get the final (i.e., if there were retries) exit code of the process.
	 *
	 * @since 1.0
	 */
	public int getExitCode() {
	    return exitCode;
	}

	/**
	 * Get the raw data collected from the process stdout.
	 *
	 * @since 1.0
	 */
	public byte[] getData() {
	    return data;
	}

	/**
	 * Get the raw data collected from the process stderr.
	 *
	 * @since 1.3
	 */
	public byte[] getError() {
	    return err;
	}

	/**
	 * Guaranteed to have at least one entry.  Since 1.3.5, if there was nothing printed to stdout, this
	 * method returns any output printed to stderr.
	 *
	 * @since 1.0
	 */
	public List<String> getLines() throws IOException {
	    List<String> lines = toLines(data);
	    if (lines.size() == 0) {
		logger.debug(Message.WARNING_MISSING_OUTPUT, cmd, exitCode, data.length);
		if (err != null && err.length > 0) {
		    lines = toLines(err);
		    if (lines.size() > 0) {
			return lines;
		    }
		}
		lines.add("");
	    }
	    return lines;
	}

	// Private

	private List<String> toLines(byte[] buff) throws IOException {
	    return Strings.toList(new LineIterator(new ByteArrayInputStream(buff)));
	}
    }

    // Private

    private static int counter = 0;

    private String cmd, dir;
    private String[] env;
    private IComputerSystem sys;
    private ExecData result;
    private long readTimeout;
    private int execRetries = 0;

    private SafeCLI(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws IOException {
	this.cmd = cmd;
	this.env = env;
	this.dir = dir;
	this.sys = sys;
	this.readTimeout = readTimeout;
	execRetries = sys.getProperties().getIntProperty(IComputerSystem.PROP_EXEC_RETRIES);
	result = new ExecData(cmd, sys.getLogger());
    }

    private ExecData getResult() {
	return result;
    }

    private void exec(IReaderHandler outputHandler, IReaderHandler errorHandler) throws IOException {
	for (int attempt=1; true; attempt++) {
	    if (execOnce(outputHandler, errorHandler, attempt)) {
		break;
	    }
	}
    }

    private boolean execOnce(IReaderHandler outputHandler, IReaderHandler errorHandler, int attempt) throws IOException {
	if (attempt > 1) {
	    sys.getLogger().info(Message.STATUS_PROCESS_RETRY, cmd);
	}
	IProcess p = null;
	PerishableReader reader = null;
	HandlerThread errThread = null;
	try {
	    p = sys.createProcess(cmd, env, dir);
	    p.start();
	    InputStream err = p.getErrorStream();
	    if (outputHandler == null) {
		if (err == null) {
		    //
		    // In this scenario, e.g., the output is redirected to a file, and a pseudo-terminal precludes the existence
		    // of a stderr channel. Hence, we use the errorHandler to process stdout.
		    //
		    outputHandler = errorHandler;
		} else {
		    outputHandler = new DevNullHandler();
		}
	    }
	    if (err == null) {
		// no errThread
	    } else if (errorHandler == null) {
		errThread = new HandlerThread(DevNull, "pipe to /dev/null", sys.getLogger());
		errThread.start(PerishableReader.newInstance(err, sys.getTimeout(Timeout.XL)));
	    } else {
		errThread = new HandlerThread(errorHandler, "stderr reader", sys.getLogger());
		errThread.start(PerishableReader.newInstance(err, sys.getTimeout(Timeout.XL)));
	    }
	    reader = PerishableReader.newInstance(p.getInputStream(), readTimeout);
	    reader.setLogger(sys.getLogger());
	    outputHandler.handle(reader);
	    p.waitFor(sys.getTimeout(Timeout.S));
	    result.exitCode = p.exitValue();
	    return true;
	} catch (IOException e) {
	    if (e instanceof InterruptedIOException || e instanceof EOFException || e instanceof SocketException) {
		if (attempt > execRetries) {
		    throw new IOException(Message.getMessage(Message.ERROR_PROCESS_RETRY, cmd, attempt), e);
		} else {
		    // the process has hung up, so kill it
		    p.destroy();
		    p = null;
		}
		return false;
	    } else {
		throw e;
	    }
	} catch (SessionException e) {
	    if (attempt > execRetries) {
		sys.getLogger().warn(Message.ERROR_PROCESS_RETRY, cmd, attempt);
		throw e;
	    } else {
		sys.getLogger().warn(Message.ERROR_SESSION_INTEGRITY, e.getMessage());
		sys.disconnect();
		return false;
	    }
	} catch (IllegalThreadStateException e) {
	    throw new IOException(e);
	} catch (InterruptedException e) {
	    throw new IOException(e);
	} finally {
	    if (p != null && p.isRunning()) {
		p.destroy();
	    } else {
		if (reader != null) {
		    try {
			reader.close();
		    } catch (IOException e) {
		    }
		}
		if (errThread != null) {
		    try {
			errThread.close();
		    } catch (IOException e) {
		    }
		}
	    }
	    if (errThread != null && errThread.isAlive()) {
		try {
		    errThread.join(1000L);
		} catch (InterruptedException e) {
		}
	    }
	}
    }

    private void exec() throws IOException {
	int maxLen = sys.getProperties().getIntProperty(IComputerSystem.PROP_PROCESS_MAXBUFFLEN);
	BufferHandler out = new BufferHandler(maxLen);
	BufferHandler err = new BufferHandler(maxLen);
	exec(out, err);
	result.data = out.getData();
	result.err = err.getData();
    }

    /**
     * An IReaderHandler that simply buffers data.
     */
    static class BufferHandler implements IReaderHandler {
	private ByteArrayOutputStream buff;
	private int maxLen;

	BufferHandler(int maxLen) {
	    buff = new ByteArrayOutputStream();
	    this.maxLen = maxLen;
	}

	byte[] getData() {
	    if (buff == null) {
		return null;
	    } else {
		return buff.toByteArray();
	    }
	}

	public void handle(IReader reader) throws IOException {
	    Streams.copy(new TruncatedInputStream(reader.getStream(), maxLen), buff, true);
	}
    }

    /**
     * An IReaderHandler that discards data.
     */
    static class DevNullHandler implements IReaderHandler {
	DevNullHandler() {
	}

	public void handle(IReader reader) throws IOException {
	    Streams.copy(reader.getStream(), Streams.devNull());
	}
    }

    static class HandlerThread implements Runnable {
	Thread thread;
	String name;
	IReader reader;
	IReaderHandler handler;
	LocLogger logger;

	HandlerThread(IReaderHandler handler, String name, LocLogger logger) {
	    this.handler = handler;
	    this.name = "ReaderHandler " + counter++ + ": " + name;
	    this.logger = logger;
	}

	void start(IReader reader) throws IllegalStateException {
	    if (thread == null || !thread.isAlive()) {
		this.reader = reader;
		thread = new Thread(Thread.currentThread().getThreadGroup(), this, name);
		thread.start();
	    } else {
		throw new IllegalStateException("running");
	    }
	}

	void close() throws IOException {
	    if (reader != null) {
		reader.close();
	    }
	}

	boolean isAlive() {
	    if (thread == null) {
		return false;
	    } else {
		return thread.isAlive();
	    }
	}

	void join() throws InterruptedException {
	    join(0L);
	}

	void join(long millis) throws InterruptedException {
	    if (thread != null) {
		thread.join(millis);
	    }
	}

	// Implement Runnable

	public void run() {
	    try {
		handler.handle(reader);
	    } catch (IOException e) {
		if (!reader.checkClosed()) {
		    logger.warn(Message.WARNING_READER_THREAD, name, e.getMessage() == null ? e.getClass().getName() : e.getMessage());
		    logger.warn(Message.ERROR_EXCEPTION, e);
		}
	    }
	}
    }

    static class ErrorLogger implements IReaderHandler {
	private LocLogger logger;

	ErrorLogger(IComputerSystem sys) {
	    logger = sys.getLogger();
	}

	ErrorLogger(LocLogger logger) {
	    this.logger = logger;
	}

	// Implement IReaderHandler

	public void handle(IReader reader) throws IOException {
	    String line = null;
	    while((line = reader.readLine()) != null) {
		if (line.length() > 0) {
		    logger.warn(Message.WARNING_COMMAND_OUTPUT, line);
		}
	    }
	}
    }

    static class FileMonitor implements Runnable {
	private IFilesystem fs;
	private String path;

	FileMonitor(IFilesystem fs) {
	    this.fs = fs;
	    this.path = null;
	}

	public synchronized void setPath(String path) {
	    fs.getLogger().debug(Message.STATUS_COMMAND_OUTPUT_TEMP, path);
	    this.path = path;
	}

	public synchronized void run() {
	    if (path != null) {
		try {
		    long len = fs.getFile(path, IFile.Flags.READVOLATILE).length();
		    fs.getLogger().info(Message.STATUS_COMMAND_OUTPUT_PROGRESS, len);
		} catch (IOException e) {
		}
	    }
	}
    }

    static class OutputLineIterator implements Iterator<String> {
	private IProcess p;
	private PerishableReader in;
	private String line;

	OutputLineIterator(IProcess p, long timeout) throws IOException {
	    this.p = p;
	    in = PerishableReader.newInstance(p.getInputStream(), timeout);
	}

	// Implement Iterator<String>

	public synchronized boolean hasNext() {
	    if (line == null) {
		if (in.checkEOF()) {
		    return false;
		} else {
		    try {
			return (line = in.readLine(Strings.UTF8)) != null;
		    } catch (IOException e) {
			if (p.isRunning()) {
			    p.destroy();
			}
			return false;
		    }
		}
	    } else {
		return true;
	    }
	}

	public synchronized String next() {
	    if (line == null) {
		throw new NoSuchElementException();
	    } else {
		String temp = line;
		line = null;
		return temp;
	    }
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
}
