// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io.driver;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.ArrayList;
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
    private static final String PRINTF = " -printf \"%M\\0%Z\\0%U\\0%G\\0%s\\0%A@\\0%C@\\0%T@\\0%p\\0%l\\n\"";
    private static final String WILDCARD = ".*";

    public LinuxDriver(IUnixSession session) {
	super(session);
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

    public String getFindCommand(List<ISearchable.ICondition> conditions) {
	String from = null;
	boolean dirOnly = false;
	boolean followLinks = false;
	boolean xdev = false;
	Pattern path = null, dirname = null, basename = null;
	String literalBasename = null, antiBasename = null, fsType = null;
	int depth = ISearchable.DEPTH_UNLIMITED;

	for (ISearchable.ICondition condition : conditions) {
	    switch(condition.getField()) {
	      case IUnixFilesystem.FIELD_FOLLOW_LINKS:
		followLinks = true;
		break;
	      case IUnixFilesystem.FIELD_XDEV:
		xdev = true;
		break;
	      case IFilesystem.FIELD_FILETYPE:
		if (IFilesystem.FILETYPE_DIR.equals(condition.getValue())) {
		    dirOnly = true;
		}
		break;
	      case IFilesystem.FIELD_PATH:
		path = (Pattern)condition.getValue();
		break;
	      case IFilesystem.FIELD_DIRNAME:
		dirname = (Pattern)condition.getValue();
		break;
	      case IFilesystem.FIELD_BASENAME:
		switch(condition.getType()) {
		  case ISearchable.TYPE_EQUALITY:
		    literalBasename = (String)condition.getValue();
		    break;
		  case ISearchable.TYPE_INEQUALITY:
		    antiBasename = (String)condition.getValue();
		    break;
		  case ISearchable.TYPE_PATTERN:
		    basename = (Pattern)condition.getValue();
		    break;
		}
		break;
	      case IFilesystem.FIELD_FSTYPE:
		fsType = (String)condition.getValue();
		break;
	      case ISearchable.FIELD_DEPTH:
		depth = ((Integer)condition.getValue()).intValue();
		break;
	      case ISearchable.FIELD_FROM:
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
	if (depth != ISearchable.DEPTH_UNLIMITED) {
	    cmd.append(" -maxdepth ").append(Integer.toString(depth));
	}

	if (dirOnly) {
	    cmd.append(" -type d");
	    cmd.append(PRINTF);
	    if (dirname != null) {
		if (!dirname.pattern().equals(WILDCARD)) {
		    cmd.append(" | awk --posix -F\\\\0 '$9 ~ /").append(escape(dirname)).append("/'");
		}
	    }
	} else {
	    if (path != null) {
		cmd.append(PRINTF);
		if (!path.pattern().equals(WILDCARD)) {
		    cmd.append(" | awk --posix -F\\\\0 '$9 ~ /").append(escape(path)).append("/'");
		}
	    } else {
		if (dirname != null) {
		    cmd.append(" -type d");
		    cmd.append(" | grep -E '").append(dirname.pattern()).append("'");
		    cmd.append(" | xargs -I{} ").append(FIND).append(" '{}' -maxdepth 1");
		}
		cmd.append(" -type f");
		if (basename != null) {
		    cmd.append(PRINTF);
		    if (!basename.pattern().equals(WILDCARD)) {
			cmd.append(" | awk --posix -F\\\\0 '{n=split($9,a,\"/\");if(match(a[n],\"");
			cmd.append(basename.pattern());
			cmd.append("\") > 0) print $0}'");
		    }
		} else if (antiBasename != null) {
		    cmd.append(" ! -name '").append(antiBasename).append("'");
		    cmd.append(PRINTF);
		} else if (literalBasename != null) {
		    cmd.append(" -name '").append(literalBasename).append("'");
		    cmd.append(PRINTF);
		}
	    }
	}
	return cmd.toString();
    }

    public String getStatCommand(String path) {
	return new StringBuffer("find '").append(path).append("'").append(PRINTF).append(" -prune").toString();
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
	if (tok.countTokens() < 9) {
	    return nextFileInfo(lines);
	} else {
	    String permissions = tok.nextToken();
	    char unixType = permissions.charAt(0);
	    permissions = permissions.substring(1);
	    boolean hasExtendedAcl = false;

	    String selinux = tok.nextToken();
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

	    long atime = IFile.UNKNOWN_TIME;
	    try {
		 atime = new BigDecimal(tok.nextToken()).movePointRight(3).longValue();
	    } catch (NumberFormatException e) {
	    }

	    long ctime = IFile.UNKNOWN_TIME;
	    try {
		 ctime = new BigDecimal(tok.nextToken()).movePointRight(3).longValue();
	    } catch (NumberFormatException e) {
	    }

	    long mtime = IFile.UNKNOWN_TIME;
	    try {
		 mtime = new BigDecimal(tok.nextToken()).movePointRight(3).longValue();
	    } catch (NumberFormatException e) {
	    }

	    String path = tok.nextToken();
	    String linkPath = null;
	    if (tok.hasMoreTokens()) {
		linkPath = tok.nextToken();
	    }

	    Properties extended = new Properties();
	    extended.setProperty(IUnixFileInfo.SELINUX_DATA, selinux);

	    return new UnixFileInfo(type, path, linkPath, ctime, mtime, atime, length,
				    unixType, permissions, uid, gid, hasExtendedAcl, extended);
	}
    }

    // Private

    private String escape(Pattern p) {
	return p.pattern().replace("/", "\\/");
    }
}
