// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.unix.io.IUnixFilesystemDriver;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.io.fs.AbstractFilesystem;
import jsaf.util.SafeCLI;

/**
 * ISearchable implementation for files on Unix machines.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class UnixFileSearcher implements ISearchable<IFile>, ILoggable {
    private IUnixSession session;
    private IUnixFilesystemDriver driver;
    private AbstractFilesystem fs;
    private LocLogger logger;
    private Map<String, Collection<String>> searchMap;

    public UnixFileSearcher(IUnixSession session, IUnixFilesystemDriver driver, Map<String, Collection<String>> searchMap) {
	this.session = session;
	this.driver = driver;
	logger = session.getLogger();
	fs = (AbstractFilesystem)session.getFilesystem();
	this.searchMap = searchMap;
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
	String cmd = driver.getFindCommand(conditions);
	Collection<IFile> results = new ArrayList<IFile>();
	if (searchMap.containsKey(cmd)) {
	    for (String path : searchMap.get(cmd)) {
		results.add(fs.getFile(path));
	    }
	} else {
	    logger.debug(Message.STATUS_FS_SEARCH_START, cmd);
	    Collection<String> paths = new ArrayList<String>();
	    try {
		Iterator<String> iter = SafeCLI.manyLines(cmd, null, session);
		IFile file = null;
		while ((file = createObject(iter)) != null) {
		    String path = file.getPath();
		    logger.debug(Message.STATUS_FS_SEARCH_MATCH, path);
		    results.add(file);
		    paths.add(path);
		}
		logger.debug(Message.STATUS_FS_SEARCH_DONE, results.size(), cmd);
	    } catch (Exception e) {
		logger.warn(Message.ERROR_FS_SEARCH);
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    searchMap.put(cmd, paths);
	}
	return results;
    }

    // Private

    private IFile createObject(Iterator<String> input) {
	UnixFileInfo info = (UnixFileInfo)driver.nextFileInfo(input);
	if (info == null) {
	    return null;
	} else if (info.getPath() == null) {
	    //
	    // Skip a bad entry and try again
	    //
	    return createObject(input);
	} else {
	    return fs.createFileFromInfo(info);
	}
    }
}
