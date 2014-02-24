// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io.driver;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.io.IReader;
import jsaf.intf.unix.io.IUnixFileInfo;
import jsaf.intf.unix.io.IUnixFilesystem;
import jsaf.intf.unix.io.IUnixFilesystem.UnixFSCondition;
import jsaf.intf.unix.io.IUnixFilesystemDriver;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.util.ISearchable;
import jsaf.io.PerishableReader;
import jsaf.provider.unix.io.UnixFileInfo;
import jsaf.util.SafeCLI;
import jsaf.util.StringTools;

/**
 * IUnixFilesystemDriver implementation for Linux.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class LinuxDriver extends AbstractDriver {
    private boolean selinuxEnabled = false;
    private String printf = " -printf \"%M\\0%U\\0%G\\0%s\\0%A@\\0%C@\\0%T@\\0%p\\0%l\\n\"";

    public LinuxDriver(IUnixSession session) {
	super(session);
	try {
	    String sestatus = SafeCLI.exec("/usr/sbin/getenforce", session, IUnixSession.Timeout.S);
	    if (sestatus.equalsIgnoreCase("Enforcing") || sestatus.equalsIgnoreCase("Permissive")) {
		selinuxEnabled = true;
		printf = " -printf \"%M\\0%Z\\0%U\\0%G\\0%s\\0%A@\\0%C@\\0%T@\\0%p\\0%l\\n\"";
	    }
	} catch (Exception e) {
	    session.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	}
    }

    void getMounts() throws Exception {
	mounts = new ArrayList<IFilesystem.IMount>();
	String command = "mount | awk '{print $3}' | xargs -I{} find {} -prune -printf \"%p %F\\n\" 2>/dev/null";
	for (String line : SafeCLI.multiLine(command, session, IUnixSession.Timeout.S)) {
	    StringTokenizer tok = new StringTokenizer(line);
	    if (tok.countTokens() == 2) {
		String mountPoint = tok.nextToken();
		String fsType = tok.nextToken();
		if (mountPoint.startsWith(IUnixFilesystem.DELIM_STR)) {
		    mounts.add(new Mount(mountPoint, fsType));
		}
	    }
	}
    }

    // Implement IUnixFilesystemDriver

    public String getFindCommand(List<ISearchable.Condition> conditions) {
	boolean dirOnly=false, xdev=false, followLinks=false;
	Pattern path=null, dirname=null, basename=null;
	String from=null, literalBasename=null, antiBasename=null, fsType=null;
	int depth = UnixFSCondition.DEPTH_UNLIMITED;

	for (ISearchable.Condition condition : conditions) {
	    switch(condition.getField()) {
	      case UnixFSCondition.FIELD_FOLLOW_LINKS:
		followLinks = true;
		break;
	      case UnixFSCondition.FIELD_XDEV:
		xdev = true;
		break;
	      case UnixFSCondition.FIELD_FILETYPE:
		if (IFilesystem.FILETYPE_DIR.equals(condition.getValue())) {
		    dirOnly = true;
		}
		break;
	      case UnixFSCondition.FIELD_PATH:
		path = (Pattern)condition.getValue();
		break;
	      case UnixFSCondition.FIELD_DIRNAME:
		dirname = (Pattern)condition.getValue();
		break;
	      case UnixFSCondition.FIELD_BASENAME:
		switch(condition.getType()) {
		  case UnixFSCondition.TYPE_EQUALITY:
		    literalBasename = (String)condition.getValue();
		    break;
		  case UnixFSCondition.TYPE_INEQUALITY:
		    antiBasename = (String)condition.getValue();
		    break;
		  case UnixFSCondition.TYPE_PATTERN:
		    basename = (Pattern)condition.getValue();
		    break;
		}
		break;
	      case UnixFSCondition.FIELD_FSTYPE:
		fsType = (String)condition.getValue();
		break;
	      case UnixFSCondition.FIELD_DEPTH:
		depth = ((Integer)condition.getValue()).intValue();
		break;
	      case UnixFSCondition.FIELD_FROM:
		from = ((String)condition.getValue()).replace(" ", "\\ ");
		break;
	    }
	}

	StringBuffer sb = new StringBuffer("find");
	if (followLinks) {
	    sb.append(" -L");
	}
	String FIND = sb.toString();
	StringBuffer cmd = new StringBuffer(FIND).append(" ").append(from);
	if (xdev) {
	    cmd.append(" -mount");
	}
	if (fsType != null) {
	    cmd.append(" -fstype ").append(fsType);
	}
	if (depth != UnixFSCondition.DEPTH_UNLIMITED) {
	    cmd.append(" -maxdepth ").append(Integer.toString(depth));
	}

	if (dirOnly) {
	    cmd.append(" -type d");
	    if (dirname != null && !dirname.pattern().equals(WILDCARD)) {
		cmd.append(" -regextype posix-egrep -regex '").append(dirname.pattern()).append("'");
	    }
	    cmd.append(printf);
	} else if (path != null) {
	    if (!path.pattern().equals(WILDCARD)) {
		cmd.append(" -regextype posix-egrep -regex '").append(path.pattern()).append("'");
	    }
	    cmd.append(printf);
	} else {
	    if (dirname != null && !dirname.pattern().equals(WILDCARD)) {
		cmd.append(" -type d");
		cmd.append(" | grep -E '").append(dirname.pattern()).append("'");
		cmd.append(" | xargs -I{} ").append(FIND).append(" '{}' -maxdepth 1");
	    }
	    cmd.append(" -type f");
	    if (basename != null) {
		cmd.append(printf);
		if (!basename.pattern().equals(WILDCARD)) {
		    cmd.append(" | awk --posix -F\\\\0 '{n=split($9,a,\"/\");if(match(a[n],/");
		    cmd.append(basename.pattern());
		    cmd.append("/) > 0) print $0}'");
		}
	    } else if (antiBasename != null) {
		cmd.append(" ! -name '").append(antiBasename).append("'");
		cmd.append(printf);
	    } else if (literalBasename != null) {
		cmd.append(" -name '").append(literalBasename).append("'");
		cmd.append(printf);
	    }
	}
	return cmd.toString();
    }

    public String getStatCommand(String path) {
	return new StringBuffer("find '").append(path).append("'").append(printf).append(" -prune").toString();
    }

    private static final String NULL = new String(new byte[] {0x00}, StringTools.ASCII);

    public UnixFileInfo nextFileInfo(Iterator<String> lines) {
	String line = null;
	while (lines.hasNext()) {
	    line = lines.next().trim();
	    if (line.length() > 0) {
		break;
	    }
	}
	if (line == null || line.length() == 0) {
	    return null;
	}
	StringTokenizer tok = new StringTokenizer(line, NULL);
	if (tok.countTokens() < (selinuxEnabled ? 9 : 8)) {
	    return nextFileInfo(lines);
	} else {
	    String perms = tok.nextToken();
	    char unixType = perms.charAt(0);
	    perms = perms.substring(1);

	    Properties ext = null; // extended properties
	    if (selinuxEnabled) {
		ext = new Properties();
		ext.setProperty(IUnixFileInfo.SELINUX_DATA, tok.nextToken());
	    }

	    int uid = -1;
	    try {
		uid = Integer.parseInt(tok.nextToken());
	    } catch (NumberFormatException e) {
		//DAS -- could be, e.g., 4294967294 (illegal "nobody" value)
	    }
	    int gid = -1;
	    try {
		gid = Integer.parseInt(tok.nextToken());
	    } catch (NumberFormatException e) {
		//DAS -- could be, e.g., 4294967294 (illegal "nobody" value)
	    }

	    IFileMetadata.Type type = IFileMetadata.Type.FILE;
	    switch(unixType) {
	      case IUnixFileInfo.DIR_TYPE:
		type = IFileMetadata.Type.DIRECTORY;
		break;

	      case IUnixFileInfo.LINK_TYPE:
		type = IFileMetadata.Type.LINK;
		break;
	    }

	    long length = 0;
	    try {
		length = Long.parseLong(tok.nextToken());
	    } catch (NumberFormatException e) {
	    }

	    Date atime=null, ctime=null, mtime=null;
	    try {
		 atime = new Date(new BigDecimal(tok.nextToken()).movePointRight(3).longValue());
	    } catch (NumberFormatException e) {
	    }
	    try {
		 ctime = new Date(new BigDecimal(tok.nextToken()).movePointRight(3).longValue());
	    } catch (NumberFormatException e) {
	    }
	    try {
		 mtime = new Date(new BigDecimal(tok.nextToken()).movePointRight(3).longValue());
	    } catch (NumberFormatException e) {
	    }

	    String path = tok.nextToken();
	    String linkPath = null;
	    if (tok.hasMoreTokens()) {
		linkPath = tok.nextToken();
	    }

	    return new UnixFileInfo(type, path, linkPath, ctime, mtime, atime, length, unixType, perms, uid, gid, null, ext);
	}
    }
}
