// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jdbm.helper.Serializer;

import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.io.IWindowsFileInfo;
import jsaf.intf.windows.io.IWindowsFilesystem;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.intf.windows.wmi.ISWbemObject;
import jsaf.intf.windows.wmi.ISWbemPropertySet;
import jsaf.intf.windows.wmi.IWmiProvider;
import jsaf.io.fs.AbstractFilesystem;
import jsaf.io.fs.DefaultMetadata;
import jsaf.io.fs.IAccessor;
import jsaf.provider.windows.Timestamp;
import jsaf.provider.windows.identity.User;
import jsaf.provider.windows.wmi.WmiException;
import jsaf.util.Base64;
import jsaf.util.SafeCLI;
import jsaf.util.StringTools;

/**
 * The local IFilesystem implementation for Windows.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class WindowsFilesystem extends AbstractFilesystem implements IWindowsFilesystem {
    private static final String DRIVE_QUERY = "Select Name, DriveType from Win32_LogicalDisk";
    private static final String START = "{";
    private static final String END = "}";

    private WindowsFileSearcher searcher;
    private final IWindowsSession.View apparentView, accessorView;
    private Collection<IVolume> volumes;
    private String system32, sysWOW64, sysNative;
    private IRunspace runspace;

    public WindowsFilesystem(IWindowsSession session, IWindowsSession.View apparentView, IWindowsSession.View accessorView) {
	super(session, DELIM_STR, IWindowsSession.View._32BIT == apparentView ? "fs32" : "fs");
	this.apparentView = apparentView;
	this.accessorView = accessorView;
	String sysRoot	= session.getEnvironment().getenv("SystemRoot");
	system32	= sysRoot + DELIM_STR + "System32"  + DELIM_STR;
	sysNative	= sysRoot + DELIM_STR + "Sysnative" + DELIM_STR;
	sysWOW64	= sysRoot + DELIM_STR + "SysWOW64"  + DELIM_STR;
    }

    protected String getAccessorPath(String path) {
	if (apparentView == accessorView) {
	    return path;
	} else if (apparentView == IWindowsSession.View._32BIT) {
	    if (path.toUpperCase().startsWith(system32.toUpperCase())) {
		return sysWOW64 + path.substring(system32.length());
	    } else if (path.toUpperCase().startsWith(sysNative.toUpperCase())) {
		return system32 + path.substring(system32.length());
	    }
	} else if (apparentView == IWindowsSession.View._64BIT) {
	    if (path.toUpperCase().startsWith(system32.toUpperCase())) {
		return sysNative + path.substring(system32.length());
	    } else if (path.toUpperCase().startsWith(sysWOW64.toUpperCase())) {
		return system32 + path.substring(sysWOW64.length());
	    }
	}
	return path;
    }

    protected IRunspace getRunspace() throws Exception {
	if (runspace == null || !runspace.isAlive()) {
	    for (IRunspace rs : ((IWindowsSession)session).getRunspacePool().enumerate()) {
		if (rs.getView() == apparentView) {
		    runspace = rs;
		}
	    }
	    if (runspace == null) {
		runspace = ((IWindowsSession)session).getRunspacePool().spawn(apparentView);
	    }
	    runspace.loadAssembly(getClass().getResourceAsStream("WindowsFilesystem.dll"));
	    runspace.loadModule(getClass().getResourceAsStream("WindowsFilesystem.psm1"));
	    runspace.loadModule(getClass().getResourceAsStream("WindowsFileSearcher.psm1"));
	}
	return runspace;
    }

    public synchronized ISearchable<IFile> getSearcher() throws IOException {
	if (searcher == null) {
	    try {
		searcher = new WindowsFileSearcher(this, getSearchCache());
	    } catch (IOException e) {
		throw e;
	    } catch (Exception e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		throw new IOException(e);
	    }
	}
	return searcher;
    }

    @Override
    public void setLogger(LocLogger logger) {
	super.setLogger(logger);
	if (searcher != null) {
	    searcher.setLogger(logger);
	}
    }

    public Collection<IMount> getMounts(Pattern filter, boolean include) throws IOException {
	try {
	    if (volumes == null) {
		volumes = new ArrayList<IVolume>();
		IWmiProvider wmi = ((IWindowsSession)session).getWmiProvider();
		for (ISWbemObject obj : wmi.execQuery(IWmiProvider.CIMv2, DRIVE_QUERY)) {
		    IVolume volume = new WindowsVolume(obj);
		    volumes.add(volume);
		}
	    }
	    if (filter == null) {
		Collection<IMount> result = new ArrayList<IMount>();
		for (IVolume volume : volumes) {
		    logger.debug(Message.STATUS_FS_MOUNT_ADD, volume.getPath(), volume.getType());
		    result.add(volume);
		}
		return result;
	    } else {
		Collection<IMount> result = new ArrayList<IMount>();
		for (IVolume volume : volumes) {
		    if (filter.matcher(volume.getType()).find()) {
			if (include) {
			    logger.debug(Message.STATUS_FS_MOUNT_ADD, volume.getPath(), volume.getType());
			    result.add(volume);
			} else {
			    logger.debug(Message.STATUS_FS_MOUNT_SKIP, volume.getPath(), volume.getType());
			}
		    } else {
			if (include) {
			    logger.debug(Message.STATUS_FS_MOUNT_SKIP, volume.getPath(), volume.getType());
			} else {
			    logger.debug(Message.STATUS_FS_MOUNT_ADD, volume.getPath(), volume.getType());
			    result.add(volume);
			}
		    }
		}
		return result;
	    }
	} catch (Exception e) {
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    throw new IOException(e.getMessage());
	}
    }

    public Serializer getFileSerializer(AbstractFilesystem fs) {
	return new WindowsFileSerializer(fs);
    }

    @Override
    protected IFile createFileFromInfo(IFileMetadata info, IFile.Flags flags) {
	if (info instanceof WindowsFileInfo) {
	    return new WindowsFile((WindowsFileInfo)info, flags);
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
		    sb.append(",");
		}
		sb.append("\"").append(path).append("\"");
	    }
	    sb.append(" | Print-FileInfo | Transfer-Encode");
	    long timeout = session.getTimeout(IWindowsSession.Timeout.M) + (30 * paths.length);
	    String data = new String(Base64.decode(runspace.invoke(sb.toString(), timeout)), StringTools.UTF8);
	    Map<String, IFile> fileMap = new HashMap<String, IFile>();
	    Iterator<String> iter = Arrays.asList(data.split("\r\n")).iterator();
	    IWindowsFileInfo info = null;
	    while ((info = nextFileInfo(iter)) != null) {
		switch(info.getWindowsFileType()) {
		  case IWindowsFileInfo.FILE_TYPE_UNKNOWN:
		    break;
		  default:
		    IFile f = createFileFromInfo((IFileMetadata)info, flags);
		    fileMap.put(f.getPath().toLowerCase(), f);
		    break;
		}
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
	return new WindowsFile(path, new File(getAccessorPath(path)), flags);
    }

    protected WindowsFileInfo getWindowsFileInfo(String path) throws IOException {
	try {
	    StringBuffer sb = new StringBuffer("Get-Item -literalPath '");
	    sb.append(path).append("' | Print-FileInfo | Transfer-Encode");
	    String data = new String(Base64.decode(runspace.invoke(sb.toString())), StringTools.UTF8);
	    return (WindowsFileInfo)nextFileInfo(StringTools.toList(data.split("\r\n")).iterator());
	} catch (IOException e) {
	    throw e;
	} catch (Exception e) {
	    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    throw new IOException(e.getMessage());
	}
    }

    protected IWindowsFileInfo nextFileInfo(Iterator<String> input) {
	boolean start = false;
	while(input.hasNext()) {
	    String line = input.next();
	    if (line.trim().equals(START)) {
		start = true;
		break;
	    }
	}
	if (start) {
	    Date ctime=null, mtime=null, atime=null;
	    long len=-1L;
	    IFileMetadata.Type type = IFileMetadata.Type.FILE;
	    int winType = IWindowsFileInfo.FILE_TYPE_UNKNOWN;
	    Map<String, String> pe = null;
	    String path = null, ownerSid = null, ownerAccount = null;

	    while(input.hasNext()) {
		String line = input.next().trim();
		if (line.equals(END)) {
		    break;
		} else if (line.equals("Type: File")) {
		    winType = IWindowsFileInfo.FILE_TYPE_DISK;
		} else if (line.equals("Type: Directory")) {
		    type = IFileMetadata.Type.DIRECTORY;
		    winType = IWindowsFileInfo.FILE_ATTRIBUTE_DIRECTORY;
		} else {
		    int ptr = line.indexOf(":");
		    if (ptr > 0) {
			String key = line.substring(0,ptr).trim();
			String val = line.substring(ptr+1).trim();
			if ("Path".equals(key)) {
			    path = val;
			} else if ("Owner.SID".equals(key)) {
			    ownerAccount = val;
			} else if ("Owner.Account".equals(key)) {
			    ownerSid = val;
			} else if (key.startsWith("pe.")) {
			    if (pe == null) {
				pe = new HashMap<String, String>();
			    }
			    String header = key.substring(3);
			    pe.put(header, val);
			} else {
			    try {
				if ("Ctime".equals(key)) {
				    ctime = new Date(Timestamp.getTime(new BigInteger(val)));
				} else if ("Mtime".equals(key)) {
				    mtime = new Date(Timestamp.getTime(new BigInteger(val)));
				} else if ("Atime".equals(key)) {
				    atime = new Date(Timestamp.getTime(new BigInteger(val)));
				} else if ("Length".equals(key)) {
				    len = Long.parseLong(val);
				} else if ("WinType".equals(key)) {
				    winType = Integer.parseInt(val);
				}
			    } catch (IllegalArgumentException e) {
				logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
			    }
			}
		    }
		}
	    }
	    IUser owner = null;
	    if (ownerAccount != null && ownerSid != null) {
		owner = new User((IWindowsSession)session, ownerAccount, ownerSid);
	    }
	    return new WindowsFileInfo(type, path, path, ctime, mtime, atime, len, winType, owner, pe);
	}
	return null;
    }

    // Private

    class WindowsVolume implements IVolume {
	private String path;
	private FsType type;
	private Integer flags;

	/**
	 * Create a new mount given a drive string and a type.
	 */
	public WindowsVolume(ISWbemObject obj) throws WmiException {
	    ISWbemPropertySet props = obj.getProperties();
	    path = props.getItem("Name").getValueAsString() + DELIM_STR;
	    type = FsType.typeOf(props.getItem("DriveType").getValueAsInteger().intValue());
	    flags = null;
	}

	// Implement IMount

	public String getPath() {
	    return path;
	}

	public String getType() {
	    return type.value();
	}

	// Implement IVolume

	public int getFlags() throws IOException {
	    if (flags == null) {
		try {
		    flags = new Integer(Integer.parseInt(runspace.invoke("Get-VolumeFlags -Path '" + path + "'").trim(), 16));
		} catch (Exception e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		    throw new IOException(e.getMessage());
		}
	    }
	    return flags.intValue();
	}
    }

    class WindowsFile extends DefaultFile {
	WindowsFile(String path, File file, IFile.Flags flags) {
	    super(path, new WindowsAccessor(path, file), flags);
	}

	WindowsFile(WindowsFileInfo info, Flags flags) {
	    super(info, flags);
	}

	@Override
	public String toString() {
	    return getAccessorPath(path);
	}

	@Override
	protected IAccessor getAccessor() {
	    if (accessor == null) {
		accessor = new WindowsAccessor(path, new File(getAccessorPath(path)));
	    }
	    return accessor;
	}
    }

    class WindowsAccessor extends DefaultAccessor {
	/**
	 * Due to 32-bit redirection, the path can differ from the underlying File's path.
	 */
	private String path;

	WindowsAccessor(String path, File file) {
	    super(file);
	    this.path = path;
	}

	@Override
	public DefaultMetadata getInfo() throws IOException {
	    if (exists()) {
		DefaultMetadata result = getWindowsFileInfo(path);
		if (result == null) {
		    result = super.getInfo();
		}
		return result;
	    } else {
		throw new FileNotFoundException(path);
	    }
	}
    }
}
