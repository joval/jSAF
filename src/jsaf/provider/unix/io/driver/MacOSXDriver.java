// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io.driver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
 * IUnixFilesystemDriver implementation for Mac OS X.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class MacOSXDriver extends AbstractDriver {
    private static final String STAT = "ls -ldnT";

    public MacOSXDriver(IUnixSession session) {
	super(session);
    }

    static final String OPEN = "(";
    static final String CLOSE = ")";

    void getMounts() throws Exception {
	mounts = new ArrayList<IFilesystem.IMount>();
	for (String line : SafeCLI.multiLine("mount", session, IUnixSession.Timeout.S)) {
	    if (line.trim().length() == 0) continue;
	    int ptr = line.indexOf(" on ");
	    String device = line.substring(0, ptr);
	    int begin = ptr + 4;
	    ptr = line.indexOf(OPEN);
	    String path = line.substring(begin, ptr).trim();
	    begin = ptr;
	    ptr = line.indexOf(CLOSE, begin);
	    StringTokenizer tok = new StringTokenizer(line.substring(begin, ptr), ",");
	    String fsType = null;
	    while(tok.hasMoreTokens()) {
		String attr = tok.nextToken().trim();
		if (attr.endsWith("fs")) {
		    fsType = attr;
		    break;
		}
	    }
	    if (fsType != null && path.startsWith(IUnixFilesystem.DELIM_STR)) {
		mounts.add(new Mount(path, fsType));
	    }
	}
    }

    // Implement IUnixFilesystemDriver

    public String getFindCommand(List<ISearchable.Condition> conditions) {
	boolean dirOnly=false, xdev=false, followLinks = false;
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
		  case UnixFSCondition.TYPE_INEQUALITY:
		    antiBasename = (String)condition.getValue();
		    break;
		  case UnixFSCondition.TYPE_EQUALITY:
		    literalBasename = (String)condition.getValue();
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
	StringBuffer cmd = new StringBuffer(FIND).append(" -E");
	if (xdev) {
	    cmd.append("x");
	}
	cmd.append(" ").append(from);
	if (fsType != null) {
	    cmd.append(" -fstype ").append(fsType);
	}
	if (depth != UnixFSCondition.DEPTH_UNLIMITED) {
	    cmd.append(" -maxdepth ").append(Integer.toString(depth));
	}

	if (dirOnly) {
	    cmd.append(" -type d");
	    if (dirname != null && !dirname.pattern().equals(WILDCARD)) {
		cmd.append(" -regex '").append(dirname.pattern()).append("'");
	    }
	} else if (path != null) {
	    if (!path.pattern().equals(WILDCARD)) {
		cmd.append(" -regex '").append(path.pattern()).append("'");
	    }
	} else {
	    if (dirname != null && !dirname.pattern().equals(WILDCARD)) {
		cmd.append(" -type d");
		cmd.append(" -regex '").append(dirname.pattern()).append("'");
		cmd.append(" -print0 | xargs -0 -I{} ").append(FIND).append(" '{}' -maxdepth 1");
	    }
	    cmd.append(" -type f");
	    if (basename != null) {
		cmd.append(" | awk -F/ '$NF ~ /");
		cmd.append(basename.pattern());
		cmd.append("/'");
	    } else if (antiBasename != null) {
		cmd.append(" ! -name '").append(antiBasename).append("'");
	    } else if (literalBasename != null) {
		cmd.append(" -name '").append(literalBasename).append("'");
	    }
	}
	cmd.append(" | xargs -I{} ").append(STAT).append(" '{}'");
	return cmd.toString();
    }

    public String getStatCommand(String path) {
	return new StringBuffer(STAT).append(" '").append(path).append("'").toString();
    }

    public UnixFileInfo nextFileInfo(Iterator<String> lines) {
	String line = null;
	if (lines.hasNext()) {
	    line = lines.next();
	} else {
	    return null;
	}
	if (line.trim().length() == 0) {
	    return nextFileInfo(lines);
	}

	char unixType = line.charAt(0);
	String perms = line.substring(1, 10);
	Boolean hasAcl = Boolean.FALSE;
	if (line.charAt(10) == '+') {
	    hasAcl = true;
	}

	StringTokenizer tok = new StringTokenizer(line.substring(11));
	String linkCount = tok.nextToken();
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

	  case IUnixFileInfo.CHAR_TYPE:
	  case IUnixFileInfo.BLOCK_TYPE:
	    int ptr = -1;
	    if ((ptr = line.indexOf(",")) > 11) {
		tok = new StringTokenizer(line.substring(ptr+1));
	    }
	    break;
	}

	long length = 0;
	try {
	    length = Long.parseLong(tok.nextToken());
	} catch (NumberFormatException e) {
	}

	Date mtime = null;
	String dateStr = tok.nextToken("/").trim();
	try {
	    mtime = new SimpleDateFormat("MMM dd HH:mm:ss yyyy").parse(dateStr);
	} catch (ParseException e) {
	    e.printStackTrace();
	}

	String path = null, linkPath = null;
	int begin = line.indexOf(session.getFilesystem().getDelimiter());
	if (begin > 0) {
	    int end = line.indexOf("->");
	    if (end == -1) {
		path = line.substring(begin).trim();
	    } else if (end > begin) {
		path = line.substring(begin, end).trim();
		linkPath = line.substring(end+2).trim();
	    }
	}

	return new UnixFileInfo(type, path, linkPath, null, mtime, null, length, unixType, perms, uid, gid, hasAcl, null);
    }
}
