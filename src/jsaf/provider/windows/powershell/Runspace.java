// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.powershell;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.system.IProcess;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.io.StreamLogger;
import jsaf.io.StreamTool;
import jsaf.util.Base64;
import jsaf.util.Checksum;
import jsaf.util.StringTools;

/**
 * A process-based implementation of an IRunspace.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Runspace implements IRunspace {
    public static final String INIT_COMMAND = "powershell -NoProfile -File -";

    private long timeout;		// contains default timeout
    private String id, prompt;
    private StringBuffer err;
    private LocLogger logger;
    private IWindowsSession.View view;
    private IProcess p;
    private InputStream stdout, stderr;	// Output from the powershell process
    private OutputStream stdin;		// Input to the powershell process
    private HashSet<String> modules, assemblies;
    private Charset encoding = null;
    private boolean buffered;
    private Character notBOM = null;

    /**
     * Create a new Runspace, using the specified architecture (null for default) and encoding.
     */
    public Runspace(String id, IWindowsSession session, IWindowsSession.View view, Charset encoding, boolean buffered)
		throws Exception {

	this.id = id;
	this.timeout = session.getTimeout(IWindowsSession.Timeout.M);
	this.logger = session.getLogger();
	this.view = view;
	this.encoding = encoding;
	this.buffered = buffered;
	modules = new HashSet<String>();
	assemblies = new HashSet<String>();
	if (view == IWindowsSession.View._32BIT && session.getNativeView() == IWindowsSession.View._64BIT) {
	    String cmd = new StringBuffer("%SystemRoot%\\SysWOW64\\cmd.exe /c ").append(INIT_COMMAND).toString();
	    p = session.createProcess(cmd, null, null);
	} else {
	    p = session.createProcess(INIT_COMMAND, null, null);
	}
	p.start();
	stdout = p.getInputStream();
	stderr = p.getErrorStream();
	stdin = p.getOutputStream();
	err = null;
	readBOM();
	read(timeout);
	notBOM = null;
    }

    public IProcess getProcess() {
	return p;
    }

    // Implement ILoggable

    public LocLogger getLogger() {
	return logger;
    }

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    // Implement IRunspace

    public String getId() {
	return id;
    }

    public void loadModule(InputStream in) throws IOException, PowershellException {
	loadModule(in, timeout);
    }

    public synchronized void loadModule(InputStream in, long millis) throws IOException, PowershellException {
	if (!p.isRunning()) {
	    throw new PowershellException(Message.getMessage(Message.ERROR_POWERSHELL_STOPPED, p.exitValue()));
	}
	try {
	    ByteArrayOutputStream buff = new ByteArrayOutputStream();
	    StreamLogger input = new StreamLogger(null, in, buff);
	    String cs = Checksum.getChecksum(input, Checksum.Algorithm.MD5);
	    input.close();
	    in = null;
	    if (modules.contains(cs)) {
		logger.debug(Message.STATUS_POWERSHELL_MODULE_SKIP, cs);
	    } else {
		logger.debug(Message.STATUS_POWERSHELL_MODULE_LOAD, cs);
		in = new ByteArrayInputStream(buff.toByteArray());
		String line = null;
		int lines = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
		while((line = reader.readLine()) != null) {
		    stdin.write(line.getBytes());
		    stdin.write("\r\n".getBytes());
		    if (!buffered) {
			stdin.flush();
			readPrompt(millis);
		    }
		    lines++;
		}
		if (buffered) {
		    if (lines > 0) {
			stdin.flush();
		    }
		    for (int i=0; i < lines; i++) {
			readPrompt(millis);
		    }
		}
		if (">> ".equals(getPrompt())) {
		    invoke("");
		}
		// DAS: add only if there was no error?
		modules.add(cs);
	    }
	} catch (TimeoutException e) {
	    throw new PowershellException(Message.getMessage(Message.ERROR_POWERSHELL_TIMEOUT));
	} finally {
	    if (in != null) {
		try {
		    in.close();
		} catch (IOException e) {
		}
	    }
	}
	if (err != null) {
	    String error = err.toString();
	    err = null;
	    throw new PowershellException(error);
	}
    }

    public void loadAssembly(InputStream in) throws IOException, PowershellException {
	loadAssembly(in, timeout);
    }

    public synchronized void loadAssembly(InputStream in, long millis) throws IOException, PowershellException {
	if (!p.isRunning()) {
	    throw new PowershellException(Message.getMessage(Message.ERROR_POWERSHELL_STOPPED, p.exitValue()));
	}
	try {
	    ByteArrayOutputStream buff = new ByteArrayOutputStream();
	    StreamLogger input = new StreamLogger(null, in, buff);
	    String cs = Checksum.getChecksum(input, Checksum.Algorithm.MD5);
	    input.close();
	    in = null;
	    if (assemblies.contains(cs)) {
		logger.debug(Message.STATUS_POWERSHELL_ASSEMBLY_SKIP, cs);
	    } else {
		logger.debug(Message.STATUS_POWERSHELL_ASSEMBLY_LOAD, cs);
		String data = Base64.encodeBytes(buff.toByteArray(), Base64.GZIP);
		invoke("Load-Assembly -Data \"" + data + "\"");
		assemblies.add(cs);
	    }
	} finally {
	    if (in != null) {
		try {
		    in.close();
		} catch (IOException e) {
		}
	    }
	}
	if (err != null) {
	    String error = err.toString();
	    err = null;
	    throw new PowershellException(error);
	}
    }

    public synchronized String invoke(String command) throws IOException, PowershellException {
	return invoke(command, timeout);
    }

    public synchronized String invoke(String command, long millis) throws IOException, PowershellException {
	if (!p.isRunning()) {
	    throw new PowershellException(Message.getMessage(Message.ERROR_POWERSHELL_STOPPED, p.exitValue()));
	}

	logger.debug(Message.STATUS_POWERSHELL_INVOKE, id, command);
	byte[] bytes = command.trim().getBytes();
	stdin.write(bytes);
	stdin.write("\r\n".getBytes());
	stdin.flush();
	try {
	    String result = read(millis);
	    if (err == null) {
		return result;
	    } else {
		String error = err.toString();
		err = null;
		throw new PowershellException(error);
	    }
	} catch (TimeoutException e) {
	    throw new PowershellException(Message.getMessage(Message.ERROR_POWERSHELL_TIMEOUT));
	}
    }

    public String getPrompt() {
	return prompt;
    }

    public IWindowsSession.View getView() {
	return view;
    }

    // Private

    /**
     * Read lines until the next prompt is reached. If there are errors, they are buffered in err.
     */
    private String read(long millis) throws IOException, TimeoutException {
	StringBuffer sb = null;
	String line = null;
	while((line = readLine(millis)) != null) {
	    if (sb == null) {
		sb = new StringBuffer();
	    } else {
		sb.append("\r\n");
	    }
	    sb.append(line);
	}
	if (sb == null) {
	    return null;
	} else {
	    return sb.toString();
	}
    }

    /**
     * Read a single line, or the next prompt. Returns null if the line is a prompt. If there are errors, they are
     * buffered in err.
     */
    private String readLine(long millis) throws IOException, TimeoutException {
	StringBuffer sb = new StringBuffer();
	//
	// Poll the streams for no more than timeout millis if there is no data.
	//
	int interval = 25;
	int max_iterations = (int)(millis / interval);
	for (int i=0; i < max_iterations; i++) {
	    int avail = 0;
	    if ((avail = stderr.available()) > 0) {
		if (err == null) {
		    err = new StringBuffer();
		}
		byte[] buff = new byte[avail];
		StreamTool.readFully(stderr, buff);
		err.append(new String(buff, encoding));
		i = 0; // reset the I/O timeout counter
	    }
	    if ((avail = stdout.available()) > 0) {
		boolean cr = false;
		while(avail-- > 0) {
		    int ch = stdout.read();
		    switch(ch) {
		      case '\r':
			cr = true;
			if (stdout.markSupported() && avail > 0) {
			    stdout.mark(1);
			    switch(stdout.read()) {
			      case '\n':
				return sb.toString();
			      default:
				stdout.reset();
				break;
			    }
			}
			break;

		      case '\n':
			return sb.toString();

		      default:
			if (cr) {
			    cr = false;
			    sb.append((char)('\r' & 0xFF));
			}
			sb.append((char)(ch & 0xFF));
		    }
		}
		if (isPrompt(sb.toString())) {
		    prompt = sb.toString();
		    return null;
		}
		i = 0; // reset the I/O timeout counter
	    }
	    if (p.isRunning()) {
		try {
		    Thread.sleep(interval);
		} catch (InterruptedException e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		    throw new IOException(e);
		}
	    } else {
		if (sb.length() > 0) {
		    return sb.toString();
		} else {
		    return null;
		}
	    }
	}
	throw new TimeoutException(Message.getMessage(Message.ERROR_POWERSHELL_TIMEOUT));
    }

    /**
     * Read a prompt. There must be NO other output to stdout, or this call will time out. Error data is buffered to err.
     */
    private synchronized void readPrompt(long millis) throws IOException, TimeoutException {
	StringBuffer sb = new StringBuffer();
	//
	// Poll the streams for no more than timeout millis if there is no data.
	//
	int interval = 25;
	int max_iterations = (int)(millis / interval);
	for (int i=0; i < max_iterations; i++) {
	    int avail = 0;
	    if ((avail = stderr.available()) > 0) {
		if (err == null) {
		    err = new StringBuffer();
		}
		byte[] buff = new byte[avail];
		stderr.read(buff);
		err.append(new String(buff, encoding));
	    }
	    if ((avail = stdout.available()) > 0) {
		boolean cr = false;
		while(avail-- > 0) {
		    sb.append((char)(stdout.read() & 0xFF));
		    if (isPrompt(sb.toString())) {
			prompt = sb.toString();
			return;
		    }
		}
		i = 0; // reset the I/O timeout counter
	    }
	    if (p.isRunning()) {
		try {
		    Thread.sleep(interval);
		} catch (InterruptedException e) {
		    throw new IOException(e);
		}
	    }
	}
	throw new TimeoutException(Message.getMessage(Message.ERROR_POWERSHELL_TIMEOUT));
    }

    private boolean isPrompt(String str) {
	if (notBOM != null) {
	    str = new StringBuffer().append(notBOM.charValue()).append(str).toString();
	}
	return (str.startsWith("PS") && str.endsWith("> ")) || str.equals(">> ");
    }

    /**
     * Attempt to read the appropriate BOM from the process stdout. In the stream doesn't start with a BOM,
     * we keep track of the first char read for use in isPrompt, until the first prompt has been read.
     */
    private void readBOM() throws IOException {
	if (encoding == StringTools.UTF8) {
	    // EE BB BF
	    int x = stdout.read();
	    switch(x) {
	      case 0xEE:
		stdout.read();
		stdout.read();
		break;
	      default:
		notBOM = new Character((char)(0xFF & x));
		break;
	    }
	} else if (encoding == StringTools.UTF16 || encoding == StringTools.UTF16LE) {
	    // FE FF (big) or FF FE (little)
	    int x = stdout.read();
	    switch(x) {
	      case 0xFE:
		stdout.read();
		break;
	      default:
		notBOM = new Character((char)(0xFF & x));
		break;
	    }
	}
    }
}
