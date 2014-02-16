// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.io;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.JSAFSystem;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.system.ISession;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.intf.windows.io.IWindowsFileInfo;
import jsaf.intf.windows.io.IWindowsFilesystem;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.io.StreamTool;
import jsaf.util.StringTools;

/**
 * An interface for searching a Windows filesystem.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
class WindowsFileSearcher implements ISearchable<IFile>, ILoggable {
    private WindowsFilesystem fs;
    private LocLogger logger;
    private Map<String, String[]> cache;

    WindowsFileSearcher(WindowsFilesystem fs, Map<String, String[]> cache) throws Exception {
	this.fs = fs;
	this.cache = cache;
	logger = fs.getSession().getLogger();
    }

    // Implement ILogger

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Implement ISearchable<IFile>

    public ICondition condition(int field, int type, Object value) {
	return new GenericCondition(field, type, value);
    }

    public String[] guessParent(Pattern p, Object... args) {
	int index = 0;
	for (Object arg : args) {
	    if (index == 0) {
		if (arg instanceof Boolean) {
		    return fs.guessParent(p, ((Boolean)arg).booleanValue());
		}
	    }
	    index++;
	}
	return fs.guessParent(p, false);
    }

    public Collection<IFile> search(List<ISearchable.ICondition> conditions) throws Exception {
	String from = null, basename = null, fsType = null;
	Pattern pathPattern = null, dirPattern = null, basenamePattern = null;
	int maxDepth = DEPTH_UNLIMITED;
	boolean dirOnly = false;
	for (ISearchable.ICondition condition : conditions) {
	    switch(condition.getField()) {
	      case FIELD_DEPTH:
		maxDepth = ((Integer)condition.getValue()).intValue();
		break;
	      case FIELD_FROM:
		from = (String)condition.getValue();
		break;
	      case IFilesystem.FIELD_PATH:
		pathPattern = (Pattern)condition.getValue();
		break;
	      case IFilesystem.FIELD_DIRNAME:
		dirPattern = (Pattern)condition.getValue();
		break;
	      case IFilesystem.FIELD_BASENAME:
		switch(condition.getType()) {
		  case ISearchable.TYPE_INEQUALITY:
		    String anti = Matcher.quoteReplacement((String)condition.getValue());
		    basenamePattern = Pattern.compile("(?!^" + anti + "$)(^.*$)");
		    break;
		  case ISearchable.TYPE_EQUALITY:
		    basename = (String)condition.getValue();
		    break;
		  case ISearchable.TYPE_PATTERN:
		    basenamePattern = (Pattern)condition.getValue();
		    break;
		}
		break;
	      case IFilesystem.FIELD_FILETYPE:
		if (IFilesystem.FILETYPE_DIR.equals(condition.getValue())) {
		    dirOnly = true;
		}
		break;
	      case IFilesystem.FIELD_FSTYPE:
		fsType = (String)condition.getValue();
		break;
	    }
	}
	if (fsType != null) {
	    //
	    // Verify that we're starting from a drive of the specified type
	    //
	    boolean ok = false;
	    for (IFilesystem.IMount mount : fs.getMounts()) {
		if (mount.getType().equals(fsType) && from.toUpperCase().startsWith(mount.getPath().toUpperCase())) {
		    ok = true;
		    break;
		}
	    }
	    if (!ok) {
		@SuppressWarnings("unchecked")
		Collection<IFile> empty = (Collection<IFile>)Collections.EMPTY_LIST;
		return empty;
	    }
	}
	StringBuffer command = null;
	if (dirOnly) {
	    command = new StringBuffer("Find-Directories -Path '").append(from).append("'");
	    if (dirPattern != null) {
		command.append(" -Pattern '").append(toString(dirPattern)).append("'");
	    }
	} else {
	    command = new StringBuffer("Find-Files -Path '").append(from).append("'");
	    if (pathPattern != null) {
		command.append(" -Pattern '").append(toString(pathPattern)).append("'");
	    }
	    if (basename != null) {
		command.append(" -LiteralFilename '").append(basename).append("'");
	    }
	    if (basenamePattern != null) {
		try {
		    String glob = StringTools.toGlob(basenamePattern);
		    logger.debug(Message.STATUS_FS_SEARCH_GLOB, basenamePattern.pattern(), glob);
		    command.append(" -FilenameGlob '").append(glob).append("'");
		} catch (IllegalArgumentException e) {
		    command.append(" -Filename '").append(toString(basenamePattern)).append("'");
		}
	    }
	}
	command.append(" -Depth ").append(Integer.toString(maxDepth));
	command.append(" | Print-FileInfo");
	String cmd = command.toString();

	Collection<IFile> results = new ArrayList<IFile>();
	if (cache.containsKey(cmd.toUpperCase())) {
	    logger.debug(Message.STATUS_FS_SEARCH_CACHED, cmd);
	    for (String path : cache.get(cmd.toUpperCase())) {
		results.add(fs.getFile(path));
	    }
	} else {
	    if (fs.getFile(from).isDirectory()) {
		logger.debug(Message.STATUS_FS_SEARCH_START, cmd);
		File localTemp = null;
		IFile remoteTemp = null;
		InputStream in = null;
		Collection<String> paths = new ArrayList<String>();
		try {
		    //
		    // Run the command on the remote host, storing the results in a temporary file, then tranfer the file
		    // locally and read it.
		    //
		    remoteTemp = execToFile(cmd);
		    File wsdir = fs.getSession().getWorkspace();
		    if (wsdir == null || ISession.LOCALHOST.equals(fs.getSession().getHostname())) {
			in = new GZIPInputStream(remoteTemp.getInputStream());
		    } else {
			localTemp = File.createTempFile("search", null, wsdir);
			StreamTool.copy(remoteTemp.getInputStream(), new FileOutputStream(localTemp), true);
			in = new GZIPInputStream(new FileInputStream(localTemp));
		    }
		    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StreamTool.detectEncoding(in)));
		    Iterator<String> iter = new ReaderIterator(reader);
		    IFile file = null;
		    while ((file = createObject(iter)) != null) {
			String path = file.getPath();
			logger.debug(Message.STATUS_FS_SEARCH_MATCH, path);
			results.add(file);
			paths.add(path);
		    }
		    //
		    // Store results in the cache for future use; NB: Windows search command key is not case-sensitive.
		    //
		    cache.put(cmd.toUpperCase(), paths.toArray(new String[paths.size()]));
		} catch (EOFException e) {
		    logger.warn(Message.ERROR_EOF);
		} catch (Exception e) {
		    logger.warn(Message.ERROR_FS_SEARCH);
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		} finally {
		    logger.debug(Message.STATUS_FS_SEARCH_DONE, results.size(), cmd);
		    if (in != null) {
			try {
			    in.close();
			} catch (IOException e) {
			}
		    }
		    if (localTemp != null) {
			localTemp.delete();
		    }
		    if (remoteTemp != null) {
			try {
			    remoteTemp.delete();
			} catch (Exception e) {
			    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
			}
		    }
		}
	    }
	}
	return results;
    }

    // Internal

    IFile createObject(Iterator<String> input) {
	WindowsFileInfo info = (WindowsFileInfo)((WindowsFilesystem)fs).nextFileInfo(input);
	if (info == null) {
	    return null;
	} else {
	    return fs.createFileFromInfo(info);
	}
    }

    boolean isSetFlag(int flag, int flags) {
	return flag == (flag & flags);
    }

    String toString(Pattern p) {
	return StringTools.regexPosix2Powershell(p.pattern());
    }

    // Private

    /**
     * Run the command, sending its output to a temporary file, and return the temporary file.
     */
    private IFile execToFile(String command) throws Exception {
	String unique = null;
	synchronized(this) {
	    unique = Long.toString(System.currentTimeMillis());
	    Thread.sleep(1);
	}
	String tempPath = fs.getSession().getTempDir();
	if (!tempPath.endsWith(IWindowsFilesystem.DELIM_STR)) {
	    tempPath = tempPath + IWindowsFilesystem.DELIM_STR;
	}
	tempPath = tempPath + "find." + unique + ".out";
	tempPath = fs.getSession().getEnvironment().expand(tempPath);
	logger.debug(Message.STATUS_FS_SEARCH_TEMP, tempPath);

	String cmd = new StringBuffer(command).append(" | Out-File ").append(tempPath).toString();

	FileMonitor mon = new FileMonitor(tempPath);
	JSAFSystem.getTimer().schedule(mon, 15000, 15000);
	try {
	    fs.getRunspace().invoke(cmd, fs.getSession().getTimeout(IWindowsSession.Timeout.XL));
	} finally {
	    mon.cancel();
	    JSAFSystem.getTimer().purge();
	}
	fs.getRunspace().invoke("Gzip-File " + tempPath);
	return fs.getFile(tempPath + ".gz", IFile.Flags.READWRITE);
    }

    class ReaderIterator implements Iterator<String> {
	BufferedReader reader;
	String next = null;

	ReaderIterator(BufferedReader reader) {
	    this.reader = reader;
	}

	// Implement Iterator<String>

	public boolean hasNext() {
	    if (next == null) {
		try {
		    next = next();
		    return true;
		} catch (NoSuchElementException e) {
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
    }

    /**
     * Periodically checks the length of a file, in a background thread. This gives us a clue as to whether very long
     * searches are really doing anything, or if they've died.
     */
    class FileMonitor extends TimerTask {
	private String path;

	FileMonitor(String path) {
	    this.path = path;
	}

	public void run() {
	    try {
		IFile f = fs.getFile(path, IFile.Flags.READVOLATILE);
		if (f.exists()) {
		    logger.info(Message.STATUS_FS_SEARCH_PROGRESS, f.length());
		}
	    } catch (Exception e) {
	    }
	}
    }
}
