// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import jsaf.Message;
import jsaf.intf.io.IReader;
import jsaf.intf.io.IReaderGobbler;
import jsaf.intf.system.IProcess;
import jsaf.intf.system.ISession;
import jsaf.io.PerishableReader;
import jsaf.io.StreamTool;
import jsaf.provider.SessionException;

/**
 * A tool for attempting to run a command-line repeatedly until it spits out some results.  It can only be used for commands
 * that require no input from stdin.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class SafeCLI {
    /**
     * On occasion, it is necessary to incorporate a String that originates from an un-trusted source into a command-line
     * statement.  This method verifies that the untrusted string cannot have any unintended side-effects by injecting
     * additional commands into the statement (potentially maliciously). Primarily, this is accomplished by insuring that
     * the string contains no quotes, so that it cannot terminate any enclosing quites and obtain access to the shell.
     *
     * @returns the input String (if no exception is thrown)
     *
     * @throws IllegalArgumentException if a potentially-malicious pattern has been detected.
     *
     * @since 1.0
     */
    public static String checkArgument(String arg, ISession session) throws IllegalArgumentException {
	switch(session.getType()) {
	  case WINDOWS:
            if (arg.indexOf("'") != -1 || arg.indexOf("\"") != -1) {
        	throw new IllegalArgumentException(arg);
	    }
	    break;

	  case UNIX:
            if (arg.indexOf("'") != -1 || arg.indexOf("\"") != -1 || arg.indexOf("`") != -1) {
        	throw new IllegalArgumentException(arg);
            }
	    break;
	}
	return arg;
    }

    /**
     * Run a command and get the first (non-empty) line of output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, ISession session, ISession.Timeout to) throws Exception {
	return exec(cmd, null, session, session.getTimeout(to));
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, ISession session, ISession.Timeout to) throws Exception {
	return exec(cmd, env, null, session, session.getTimeout(to));
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment and start directory.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, String dir, ISession session, ISession.Timeout to)
		throws Exception {

	return exec(cmd, env, dir, session, session.getTimeout(to));
    }

    /**
     * Run a command and get the first (non-empty) line of output.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, ISession session, long readTimeout) throws Exception {
	return exec(cmd, null, null, session, readTimeout);
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, ISession session, long readTimeout) throws Exception {
	return exec(cmd, env, null, session, readTimeout);
    }

    /**
     * Run a command and get the first (non-empty) line of output, using the specified environment and start directory.
     *
     * @since 1.0
     */
    public static final String exec(String cmd, String[] env, String dir, ISession session, long readTimeout) throws Exception {
	List<String> lines = multiLine(cmd, env, dir, session, readTimeout);
	if (lines.size() == 2 && lines.get(0).equals("")) {
	    return lines.get(1);
	} else {
	    return lines.get(0);
	}
    }

    /**
     * Run a command and get the resulting lines of output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, ISession session, ISession.Timeout to) throws Exception {
	return multiLine(cmd, null, null, session, session.getTimeout(to));
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, ISession session, ISession.Timeout to)
		throws Exception {

	return multiLine(cmd, env, null, session, session.getTimeout(to));
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment and start directory.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, String dir, ISession session, ISession.Timeout to)
		throws Exception {

	return multiLine(cmd, env, dir, session, session.getTimeout(to));
    }

    /**
     * Run a command and get the resulting lines of output.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, ISession session, long readTimeout) throws Exception {
	return multiLine(cmd, null, null, session, readTimeout);
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, ISession session, long readTimeout) throws Exception {
	return multiLine(cmd, env, null, session, readTimeout);
    }

    /**
     * Run a command and get the resulting lines of output, using the specified environment.
     *
     * @since 1.0
     */
    public static final List<String> multiLine(String cmd, String[] env, String dir, ISession session, long readTimeout)
		throws Exception {

	return execData(cmd, env, dir, session, readTimeout).getLines();
    }

    /**
     * Run a command and get the resulting ExecData, using the specified environment.
     *
     * @since 1.0
     */
    public static final ExecData execData(String cmd, String[] env, ISession session, long readTimeout) throws Exception {
	return execData(cmd, env, null, session, readTimeout);
    }

    /**
     * Run a command and get the resulting ExecData, using the specified environment and start directory.
     *
     * @since 1.0
     */
    public static final ExecData execData(String cmd, String[] env, String dir, ISession session, long readTimeout)
		throws Exception {

	SafeCLI cli = new SafeCLI(cmd, env, dir, session, readTimeout);
	cli.exec();
	return cli.getResult();
    }

    /**
     * Run a command, using the specified output processor ("gobbler").
     *
     * When the command is run, an IReader to the output is passed to the gobbler using the gobble method. If the command
     * hangs (signaled to SafeCLI by an InterruptedIOException or SessionException), the SafeCLI will kill the old command,
     * start a new command instance (up the the session's configured number of retries), and call gobbler.gobble again.
     *
     * Hence, the gobbler should initialize itself completely when gobble is invoked, and not perform permanent output
     * processing until the reader has reached the end of the process output.
     *
     * @since 1.0
     */
    public static final void exec(String cmd, String[] env, String dir, ISession session, long readTimeout,
				  IReaderGobbler out, IReaderGobbler err) throws Exception {

	new SafeCLI(cmd, env, dir, session, readTimeout).exec(out, err);
    }

    /**
     * A container for information resulting from the execution of a process.
     *
     * @since 1.0
     */
    public class ExecData {
	int exitCode;
	byte[] data;

	ExecData() {
	    exitCode = -1;
	    data = null;
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
	 * Guaranteed to have at least one entry.
	 *
	 * @since 1.0
	 */
	public List<String> getLines() throws IOException {
	    Charset encoding = null;
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    in.mark(data.length);
	    try {
		encoding = StreamTool.detectEncoding(in);
	    } catch (IOException e) {
		in.reset();
		encoding = StringTools.ASCII;
	    }
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
	    List<String> lines = new Vector<String>();
	    String line = null;
	    while((line = reader.readLine()) != null) {
		lines.add(line);
	    }
	    if (lines.size() == 0) {
		session.getLogger().debug(Message.WARNING_MISSING_OUTPUT, cmd, exitCode, data.length);
		lines.add("");
	    }
	    return lines;
	}
    }

    // Private

    private String cmd, dir;
    private String[] env;
    private ISession session;
    private ExecData result;
    private long readTimeout;
    private int execRetries = 0;

    private SafeCLI(String cmd, String[] env, String dir, ISession session, long readTimeout) throws Exception {
	this.cmd = cmd;
	this.env = env;
	this.dir = dir;
	this.session = session;
	this.readTimeout = readTimeout;
	execRetries = session.getProperties().getIntProperty(ISession.PROP_EXEC_RETRIES);
	result = new ExecData();
    }

    private ExecData getResult() {
	return result;
    }

    private void exec(IReaderGobbler outputGobbler, IReaderGobbler errorGobbler) throws Exception {
	boolean success = false;
	for (int attempt=0; !success; attempt++) {
	    IProcess p = null;
	    PerishableReader reader = null;
	    try {
		p = session.createProcess(cmd, env, dir);
		p.start();
		reader = PerishableReader.newInstance(p.getInputStream(), readTimeout);
		reader.setLogger(session.getLogger());
		if (errorGobbler != null) {
		    new GobblerThread(errorGobbler).start(PerishableReader.newInstance(p.getErrorStream(), readTimeout));
		}
		outputGobbler.gobble(reader);
		try {
		    p.waitFor(session.getTimeout(ISession.Timeout.M));
		    result.exitCode = p.exitValue();
		    success = true;
		} catch (InterruptedException e) {
		}
	    } catch (IOException e) {
		if (e instanceof InterruptedIOException || e instanceof EOFException) {
		    if (attempt > execRetries) {
			session.getLogger().warn(Message.ERROR_PROCESS_RETRY, cmd, attempt);
			throw e;
		    } else {
			// the process has hung up, so kill it
			p.destroy();
			p = null;
			session.getLogger().info(Message.STATUS_PROCESS_RETRY, cmd);
		    }
		} else {
		    throw e;
		}
	    } catch (SessionException e) {
		if (attempt > execRetries) {
		    session.getLogger().warn(Message.ERROR_PROCESS_RETRY, cmd, attempt);
		    throw e;
		} else {
		    session.getLogger().warn(Message.ERROR_SESSION_INTEGRITY, e.getMessage());
		    session.getLogger().info(Message.STATUS_PROCESS_RETRY, cmd);
		    session.disconnect();
		}
	    } finally {
		if (reader != null) {
		    try {
			reader.close();
		    } catch (IOException e) {
		    }
		}
		if (p != null) {
		    try {
			InputStream err = p.getErrorStream();
			if (err != null) {
			    err.close();
			}
		    } catch (IOException e) {
		    }
		    if (p.isRunning()) {
			p.destroy();
		    }
		}
	    }
	}
    }

    private void exec() throws Exception {
	exec(new InnerGobbler(), null);
    }

    /**
     * An IReaderGobbler that reads data into an ExecData. This internal implementation sets the ExecData result for the
     * class.
     */
    class InnerGobbler implements IReaderGobbler {
	InnerGobbler() {}

	public void gobble(IReader reader) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buff = new byte[512];
	    int len = 0;
	    while((len = reader.getStream().read(buff)) > 0) {
		out.write(buff, 0, len);
	    }
	    result.data = out.toByteArray();
	}
    }

    class GobblerThread implements Runnable {
	Thread thread;
	IReader reader;
	IReaderGobbler gobbler;

	GobblerThread(IReaderGobbler gobbler) {
	    this.gobbler = gobbler;
	}

	void start(IReader reader) throws IllegalStateException {
	    if (thread == null || !thread.isAlive()) {
		this.reader = reader;
		thread = new Thread(this);
		thread.start();
	    } else {
		throw new IllegalStateException("running");
	    }
	}

	// Implement Runnable

	public void run() {
	    try {
		gobbler.gobble(reader);
	    } catch (IOException e) {
	    }
	}
    }
}
