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
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import jsaf.JSAFSystem;
import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.io.IReader;
import jsaf.intf.system.IProcess;
import jsaf.intf.system.IComputerSystem;
import jsaf.intf.system.ISession;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.io.PerishableReader;
import jsaf.io.SimpleReader;
import jsaf.io.Streams;
import jsaf.provider.SessionException;

/**
 * A utility that simplifyies the execution of simple command-lines.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class SafeCLI {
    /**
     * An interface for processing data from a process stream (stdout or stderr), used by the exec method.
     *
     * If the command hangs diring processing (signaled to SafeCLI by an InterruptedIOException or SessionException),
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
     * Run a command and get the first (non-empty) line of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, IComputerSystem sys, ISession.Timeout readTimeout) throws Exception {
	return exec(cmd, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, IComputerSystem sys, ISession.Timeout readTimeout) throws Exception {
	return exec(cmd, env, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, String dir, IComputerSystem sys, ISession.Timeout readTimeout) throws Exception {
	return exec(cmd, env, dir, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the first (non-empty) line of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, IComputerSystem sys, long readTimeout) throws Exception {
	return exec(cmd, null, null, sys, readTimeout);
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, IComputerSystem sys, long readTimeout) throws Exception {
	return exec(cmd, env, null, sys, readTimeout);
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws Exception {
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
    public static final List<String> multiLine(String cmd, IComputerSystem sys, ISession.Timeout readTimeout) throws Exception {
	return multiLine(cmd, null, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, IComputerSystem sys, ISession.Timeout readTimeout) throws Exception {
	return multiLine(cmd, env, null, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, String dir, IComputerSystem sys, ISession.Timeout readTimeout) throws Exception {
	return multiLine(cmd, env, dir, sys, sys.getTimeout(readTimeout));
    }

    /**
     * Run a command and get the resulting lines of output.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, IComputerSystem sys, long readTimeout) throws Exception {
	return multiLine(cmd, null, null, sys, readTimeout);
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, IComputerSystem sys, long readTimeout) throws Exception {
	return multiLine(cmd, env, null, sys, readTimeout);
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws Exception {
	return execData(cmd, env, dir, sys, readTimeout).getLines();
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment. This command assumes that
     * there will be a large volume of output from the command, so it will pipe the output to a file, transfer the file
     * locally (if sys is a remote session), and then return an iterator that reads lines from the local file.
     * When the end of the iterator is reached, the local file is deleted.
     *
     * @since 1.0.1
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IUnixSession sys) throws Exception {
	return manyLines(cmd, env, new ErrorLogger(sys), sys, sys.getTimeout(ISession.Timeout.XL));
    }

    /**
     * Pass in a custom timeout to the manyLines command.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IUnixSession sys, ISession.Timeout timeout) throws Exception {
	return manyLines(cmd, env, sys, sys.getTimeout(timeout));
    }

    /**
     * Pass in a custom timeout to the manyLines command.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IUnixSession sys, long timeout) throws Exception {
	return manyLines(cmd, env, new ErrorLogger(sys), sys, timeout);
    }

    /**
     * Pass in a custom error stream handler to the manyLines command.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IReaderHandler errHandler, IUnixSession sys, ISession.Timeout timeout)
		throws Exception {

	return manyLines(cmd, env, errHandler, sys, sys.getTimeout(timeout));
    }

    /**
     * Pass in a custom error stream handler to the manyLines command, with a custom timeout.
     *
     * @param timeout Specifies the maximum time that the command should take to finish executing.
     *
     * @since 1.3
     */
    public static final Iterator<String> manyLines(String cmd, String[] env, IReaderHandler errHandler, IUnixSession sys, long timeout) throws Exception {
	//
	// If no filesystem is available, then implement using execData.
	//
	if (sys.getFilesystem() == null) {
	    ExecData ed = execData(cmd, env, null, sys, timeout);
	    errHandler.handle(new SimpleReader(new ByteArrayInputStream(ed.getError()), sys.getLogger()));
	    return ed.getLines().iterator();
	}

	//
	// Modify the command to redirect output to a temp file (compressed)
	//
	IFile remoteTemp = sys.getFilesystem().createTempFile("cmd", ".out", null);
	String tempPath = remoteTemp.getPath();
	if ((cmd.indexOf(";") != -1 || cmd.indexOf("&&") != -1) && !cmd.startsWith("(") && !cmd.endsWith(")")) {
	    //
	    // Multiple comands have to be grouped, or only the last one's output will be redirected.
	    //
	    cmd = new StringBuffer("(").append(cmd).append(")").toString();
	}
	switch(sys.getFlavor()) {
	  case HPUX:
	    cmd = new StringBuffer(cmd).append(" | /usr/contrib/bin/gzip > ").append(tempPath).toString();
	    break;
	  default:
	    cmd = new StringBuffer(cmd).append(" | gzip > ").append(tempPath).toString();
	    break;
	}

	//
	// Execute the command, and monitor the size of the output file
	//
	FileMonitor mon = new FileMonitor(sys.getFilesystem(), tempPath);
	JSAFSystem.getTimer().schedule(mon, 15000, 15000);
	try {
	    BufferHandler out = new BufferHandler();
	    BufferHandler err = new BufferHandler();
	    exec(cmd, null, null, sys, timeout, out, err);
	    byte[] buff = err.getData();
	    if (buff.length > 0) {
		errHandler.handle(new SimpleReader(new ByteArrayInputStream(buff), sys.getLogger()));
	    } else {
		//
		// Since stdout has been redirected to a file, and we've already attempted to process the buffered
		// output from stderr, any data contained by the output buffer must be stderr output that's been
		// directed for whatever reason to the stdout stream.
		//
		errHandler.handle(new SimpleReader(new ByteArrayInputStream(out.getData()), sys.getLogger()));
	    }
	} finally {
	    mon.cancel();
	    JSAFSystem.getTimer().purge();
	}

	//
	// Create and return a reader/Iterator<String> based on a local cache file
	//
	if (ISession.LOCALHOST.equals(sys.getHostname())) {
	    return new ReaderIterator(new File(tempPath));
	} else {
	    File tempDir = sys.getWorkspace() == null ? new File(System.getProperty("user.home")) : sys.getWorkspace();
	    File localTemp = File.createTempFile("cmd", null, tempDir);
	    Streams.copy(remoteTemp.getInputStream(), new FileOutputStream(localTemp), true);
	    try {
		remoteTemp.delete();
	    } catch (IOException e) {
		sys.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return new ReaderIterator(localTemp);
	}
    }

    /**
     * Run a command and get the resulting ExecData, using the specified environment.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final ExecData execData(String cmd, String[] env, IComputerSystem sys, long readTimeout) throws Exception {
	return execData(cmd, env, null, sys, readTimeout);
    }

    /**
     * Run a command and get the resulting ExecData, using the specified environment and start directory.
     *
     * @param readTimeout Specifies the maximum amount of time the command should go without producing any character output.
     *
     * @since 1.0
     */
    public static final ExecData execData(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws Exception {
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
		throws Exception {

	new SafeCLI(cmd, env, dir, sys, readTimeout).exec(out, err);
    }

    /**
     * A container for information resulting from the execution of a process.
     *
     * @since 1.0
     */
    public class ExecData {
	private int exitCode;
	private byte[] data, err;

	ExecData() {
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
		sys.getLogger().debug(Message.WARNING_MISSING_OUTPUT, cmd, exitCode, data.length);
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
	    ByteArrayInputStream in = new ByteArrayInputStream(buff);
	    in.mark(data.length);
	    Charset encoding = Strings.UTF8;
	    try {
		encoding = Streams.detectEncoding(in);
	    } catch (IOException e) {
		in.reset();
	    }
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
	    List<String> lines = new ArrayList<String>();
	    String line = null;
	    while((line = reader.readLine()) != null) {
		lines.add(line);
	    }
	    return lines;
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

    private SafeCLI(String cmd, String[] env, String dir, IComputerSystem sys, long readTimeout) throws Exception {
	this.cmd = cmd;
	this.env = env;
	this.dir = dir;
	this.sys = sys;
	this.readTimeout = readTimeout;
	execRetries = sys.getProperties().getIntProperty(IComputerSystem.PROP_EXEC_RETRIES);
	result = new ExecData();
    }

    private ExecData getResult() {
	return result;
    }

    private void exec(IReaderHandler outputHandler, IReaderHandler errorHandler) throws Exception {
	boolean success = false;
	for (int attempt=1; !success; attempt++) {
	    IProcess p = null;
	    PerishableReader reader = null;
	    HandlerThread errThread = null;
	    try {
		p = sys.createProcess(cmd, env, dir);
		p.start();
		reader = PerishableReader.newInstance(p.getInputStream(), readTimeout);
		reader.setLogger(sys.getLogger());
		if (errorHandler == null) {
		    errThread = new HandlerThread(DevNull, "pipe to /dev/null");
		    errThread.start(PerishableReader.newInstance(p.getErrorStream(), sys.getTimeout(ISession.Timeout.XL)));
		} else {
		    errThread = new HandlerThread(errorHandler, "stderr reader");
		    errThread.start(PerishableReader.newInstance(p.getErrorStream(), sys.getTimeout(ISession.Timeout.XL)));
		}
		outputHandler.handle(reader);
		p.waitFor(sys.getTimeout(ISession.Timeout.S));
		result.exitCode = p.exitValue();
		success = true;
	    } catch (IOException e) {
		if (e instanceof InterruptedIOException || e instanceof EOFException || e instanceof SocketException) {
		    if (attempt > execRetries) {
			throw new Exception(Message.getMessage(Message.ERROR_PROCESS_RETRY, cmd, attempt), e);
		    } else {
			// the process has hung up, so kill it
			p.destroy();
			p = null;
			sys.getLogger().info(Message.STATUS_PROCESS_RETRY, cmd);
		    }
		} else {
		    throw e;
		}
	    } catch (SessionException e) {
		if (attempt > execRetries) {
		    sys.getLogger().warn(Message.ERROR_PROCESS_RETRY, cmd, attempt);
		    throw e;
		} else {
		    sys.getLogger().warn(Message.ERROR_SESSION_INTEGRITY, e.getMessage());
		    sys.getLogger().info(Message.STATUS_PROCESS_RETRY, cmd);
		    sys.disconnect();
		}
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
    }

    private void exec() throws Exception {
	BufferHandler out = new BufferHandler();
	BufferHandler err = new BufferHandler();
	exec(out, err);
	result.data = out.getData();
	result.err = err.getData();
    }

    /**
     * An IReaderHandler that simply buffers data.
     */
    static class BufferHandler implements IReaderHandler {
	private ByteArrayOutputStream buff;

	BufferHandler() {
	    buff = new ByteArrayOutputStream();
	}

	byte[] getData() {
	    if (buff == null) {
		return null;
	    } else {
		return buff.toByteArray();
	    }
	}

	public void handle(IReader reader) throws IOException {
	    Streams.copy(reader.getStream(), buff, true);
	}
    }

    class HandlerThread implements Runnable {
	Thread thread;
	String name;
	IReader reader;
	IReaderHandler handler;

	HandlerThread(IReaderHandler handler, String name) {
	    this.handler = handler;
	    this.name = "ReaderHandler " + counter++ + ": " + name;
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
	    if (thread != null && thread.isAlive()) {
		thread.interrupt();
	    }
	    reader.close();
	}

	boolean isAlive() {
	    return thread.isAlive();
	}

	void join() throws InterruptedException {
	    join(0L);
	}

	void join(long millis) throws InterruptedException {
	    thread.join(millis);
	}

	// Implement Runnable

	public void run() {
	    try {
		handler.handle(reader);
	    } catch (IOException e) {
		sys.getLogger().warn(Message.ERROR_EXCEPTION, e);
	    }
	}
    }

    static class ErrorLogger implements IReaderHandler {
	private IComputerSystem sys;

	ErrorLogger(IComputerSystem sys) {
	    this.sys = sys;
	}

	// Implement IReaderHandler

	public void handle(IReader reader) throws IOException {
	    String line = null;
	    while((line = reader.readLine()) != null) {
		if (line.length() > 0) {
		    sys.getLogger().warn(Message.WARNING_COMMAND_OUTPUT, line);
		}
	    }
	}
    }

    static class FileMonitor extends TimerTask {
	private IFilesystem fs;
	private String path;

	FileMonitor(IFilesystem fs, String path) {
	    this.fs = fs;
	    this.path = path;
	    fs.getLogger().debug(Message.STATUS_COMMAND_OUTPUT_TEMP, path);
	}

	public void run() {
	    try {
		long len = fs.getFile(path, IFile.Flags.READVOLATILE).length();
		fs.getLogger().info(Message.STATUS_COMMAND_OUTPUT_PROGRESS, len);
	    } catch (IOException e) {
	    }
	}
    }

    static class ReaderIterator implements Iterator<String> {
	File file;
	BufferedReader reader;
	String next = null;

	ReaderIterator(File file) throws IOException {
	    this.file = file;
	    try {
		InputStream in = new GZIPInputStream(new FileInputStream(file));
		reader = new BufferedReader(new InputStreamReader(in, Strings.UTF8));
	    } catch (IOException e) {
		close();
		throw e;
	    }
	}

	@Override
	protected void finalize() {
	    close();
	}

	// Implement Iterator<String>

	public boolean hasNext() {
	    if (next == null) {
		try {
		    next = next();
		    return true;
		} catch (NoSuchElementException e) {
		    close();
		    return false;
		}
	    } else {
		return true;
	    }
	}

	public String next() throws NoSuchElementException {
	    if (next == null) {
		try {
		    if ((next = reader.readLine()) == null) {
			try {
			    reader.close();
			} catch (IOException e) {
			}
			throw new NoSuchElementException();
		    }
		} catch (IOException e) {
		    throw new NoSuchElementException(e.getMessage());
		}
	    }
	    String temp = next;
	    next = null;
	    return temp;
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}

	// Private

	/**
	 * Clean up the remote or local file.
	 */
	private void close() {
	    if (file != null) {
		if (file.delete()) {
		    file = null;
		}
	    }
	}
    }
}
