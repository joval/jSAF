// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jdbm.helper.Serializer;

import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.unix.io.IUnixFileInfo;
import jsaf.intf.unix.io.IUnixFilesystem;
import jsaf.intf.unix.io.IUnixFilesystemDriver;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.util.ISearchable;
import jsaf.io.fs.AbstractFilesystem;
import jsaf.io.fs.DefaultMetadata;
import jsaf.io.fs.IAccessor;
import jsaf.provider.unix.io.driver.AIXDriver;
import jsaf.provider.unix.io.driver.LinuxDriver;
import jsaf.provider.unix.io.driver.MacOSXDriver;
import jsaf.provider.unix.io.driver.SolarisDriver;
import jsaf.util.SafeCLI;
import jsaf.util.StringTools;

/**
 * A local IFilesystem implementation for Unix.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class UnixFilesystem extends AbstractFilesystem implements IUnixFilesystem {
    protected long S, M, L, XL;

    private UnixFileSearcher searcher;
    private IUnixFilesystemDriver driver;

    public UnixFilesystem(IUnixSession session) {
	super(session, DELIM_STR, "fs");
	S = session.getTimeout(IUnixSession.Timeout.S);
	M = session.getTimeout(IUnixSession.Timeout.M);
	L = session.getTimeout(IUnixSession.Timeout.L);
	XL= session.getTimeout(IUnixSession.Timeout.XL);
    }

    public ISearchable<IFile> getSearcher() {
	if (searcher == null) {
	    searcher = new UnixFileSearcher((IUnixSession)session, getDriver());
	}
	return searcher;
    }

    @Override
    public void setLogger(LocLogger logger) {
	super.setLogger(logger);
	if (searcher != null) {
	    searcher.setLogger(logger);
	}
	if (driver != null) {
	    driver.setLogger(logger);
	}
    }

    public Collection<IMount> getMounts(Pattern filter, boolean include) throws IOException {
	try {
	    return getDriver().getMounts(filter, include);
	} catch (Exception e) {
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    throw new IOException(e);
	}
    }

    public Serializer getFileSerializer(AbstractFilesystem fs) {
	return new UnixFileSerializer(fs);
    }

    @Override
    protected IFile createFileFromInfo(IFileMetadata info, IFile.Flags flags) {
	if (info instanceof UnixFileInfo) {
	    return new UnixFile((UnixFileInfo)info, flags);
	} else {
	    return super.createFileFromInfo(info, flags);
	}
    }

    @Override
    public IFile[] getFiles(String[] paths, IFile.Flags flags) throws IOException {
	try {
	    HashSet<String> uniquePaths = new HashSet<String>();
	    for (String path : paths) {
		uniquePaths.add(path);
	    }
	    StringBuffer sb = new StringBuffer();
	    for (String path : uniquePaths) {
		SafeCLI.checkArgument(path, session);
		if (sb.length() > 0) {
		    sb.append("\\n");
		}
		sb.append(path);
	    }
	    StringBuffer cmd = new StringBuffer("printf \"").append(sb.toString()).append("\\n\"");
	    cmd.append(" | xargs -I{} ").append(getDriver().getStatCommand("{}"));
	    Map<String, IFile> fileMap = new HashMap<String, IFile>();
	    Iterator<String> iter = SafeCLI.manyLines(cmd.toString(), null, (IUnixSession)session);
	    IUnixFileInfo info = null;
	    while ((info = getDriver().nextFileInfo(iter)) != null) {
		IFile f = createFileFromInfo((IFileMetadata)info, flags);
		fileMap.put(f.getPath().toLowerCase(), f);
	    }
	    IFile[] files = new IFile[paths.length];
	    for (int i=0; i < paths.length; i++) {
		files[i] = fileMap.get(paths[i].toLowerCase());
	    }
	    return files;
	} catch (IOException e) {
	    throw e;
	} catch (Exception e) {
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    throw new IOException(e.getMessage());
	}
    }

    protected IFile getPlatformFile(String path, IFile.Flags flags) throws IOException {
	return new UnixFile(new File(path), flags);
    }

    // Implement IUnixFilesystem

    public IUnixFilesystemDriver getDriver() {
	if (driver == null) {
	    IUnixSession us = (IUnixSession)session;
	    switch(us.getFlavor()) {
	      case AIX:
		driver = new AIXDriver(us);
		break;
	      case MACOSX:
		driver = new MacOSXDriver(us);
		break;
	      case LINUX:
		driver = new LinuxDriver(us);
		break;
	      case SOLARIS:
		driver = new SolarisDriver(us);
		break;
	      default:
		throw new RuntimeException(Message.getMessage(Message.ERROR_UNSUPPORTED_UNIX_FLAVOR, us.getFlavor()));
	    }
	}
	return driver;
    }

    // Internal

    protected UnixFileInfo getUnixFileInfo(String path) throws IOException {
	int exitCode = -1;
	String data = null;
	try {
	    String cmd = new StringBuffer(getDriver().getStatCommand(path)).toString();
	    SafeCLI.ExecData ed = SafeCLI.execData(cmd, null, session, S);
	    exitCode = ed.getExitCode();
	    data = new String(ed.getData(), StringTools.ASCII);
	    List<String> lines = ed.getLines();
	    UnixFileInfo ufi = null;
	    if (lines.size() > 0) {
		switch(exitCode) {
		  case -1: // couldn't determine the exit code
		  case 0:
		    ufi = (UnixFileInfo)getDriver().nextFileInfo(lines.iterator());
		    if (ufi == null) {
			if (exitCode == 0) {
			    throw new Exception(Message.getMessage(Message.ERROR_UNIXFILEINFO, path, data));
			} else {
			    throw new IOException(Message.getMessage(Message.ERROR_FS_LSTAT, path, exitCode, data));
			}
		    }
		    break;

		  //
		  // If we're here, that means that we've got a known-bad exit code for ls.
		  //
		  default:
		    StringBuffer sb = new StringBuffer();
		    for (int i=0; i < lines.size(); i++) {
			if (i > 0) {
			    sb.append(StringTools.LOCAL_CR);
			}
			sb.append(lines.get(i));
		    }
		    throw new IOException(sb.toString());
		}
	    } else {
		logger.warn(Message.ERROR_UNIXFILEINFO, path, "''");
	    }
	    return ufi;
	} catch (IOException e) {
	    throw e;
	} catch (Exception e) {
	    logger.warn(Message.ERROR_FS_LSTAT, path, exitCode, data);
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    throw new IOException(e.getMessage());
	}
    }

    protected class UnixFile extends DefaultFile {
	UnixFile(File file, IFile.Flags flags) throws IOException {
	    super(file.getPath(), new UnixAccessor(file), flags);
	}

	protected UnixFile(String path, IAccessor accessor, Flags flags) {
	    super(path, accessor, flags);
	}

	/**
	 * Create a UnixFile using information.
	 */
	protected UnixFile(UnixFileInfo info, Flags flags) {
	    super(info, flags);
	}

	@Override
	protected IAccessor getAccessor() throws IOException {
	    if (accessor == null) {
		accessor = new UnixAccessor(new File(path));
	    }
	    return accessor;
	}

	/**
	 * If this file is a link to a directory, we want this to return true.
	 */
	@Override
	public boolean isDirectory() throws IOException {
	    if (isLink()) {
		String canonicalPath = getCanonicalPath();
		if (!path.equals(canonicalPath)) {
		    return getFile(canonicalPath).isDirectory();
		} else {
		    return false; // a link to oneself is just a link
		}
	    } else {
		return super.isDirectory();
	    }
	}
    }

    class UnixAccessor extends DefaultAccessor {
	private String path;

	UnixAccessor(File file) {
	    super(file);
	    path = file.getPath();
	}

	@Override
	public DefaultMetadata getInfo() throws IOException {
	    DefaultMetadata result = getUnixFileInfo(path);
	    if (result == null) {
		result = super.getInfo();
	    }
	    return result;
	}
    }
}
