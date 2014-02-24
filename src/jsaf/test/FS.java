// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.io.IFilesystem.FSCondition;
import jsaf.intf.system.IComputerSystem;
import jsaf.intf.system.IEnvironment;
import jsaf.intf.system.ISession;
import jsaf.intf.util.ISearchable;
import jsaf.util.Checksum;

public class FS {
    private IComputerSystem session;

    public FS(ISession session) {
	this.session = (IComputerSystem)session;
    }

    public synchronized void test(String path) {
	InputStream in = null;
	try {
//IFilesystem fs = ((org.joval.intf.windows.system.IWindowsSession)session).getFilesystem(org.joval.intf.windows.system.IWindowsSession.View._32BIT);
	    IFilesystem fs = session.getFilesystem();
	    IEnvironment env = session.getEnvironment();
	    if (path.startsWith("search:")) {
		path = path.substring(7);
		Pattern pattern = Pattern.compile(path);
		Collection<IFile> list = new ArrayList<IFile>();
		ISearchable<IFile> searcher = fs.getSearcher();
		String[] from = searcher.guessParent(pattern);
		if (from == null) {
		    Collection<IFilesystem.IMount> mounts = fs.getMounts();
		    from = new String[mounts.size()];
		    int i=0;
		    for (IFilesystem.IMount mount : mounts) {
			from[i++] = mount.getPath();
		    }
		}
		for (String s : from) {
		    List<ISearchable.Condition> conditions = new ArrayList<ISearchable.Condition>();
		    conditions.add(new FSCondition(FSCondition.FIELD_FROM, FSCondition.TYPE_EQUALITY, s));
		    conditions.add(new FSCondition(FSCondition.FIELD_PATH, FSCondition.TYPE_PATTERN, pattern));
		    conditions.add(ISearchable.RECURSE);
		    list.addAll(searcher.search(conditions));
		}
		System.out.println("Found " + list.size() + " matches");
		for (IFile file : list) {
		    System.out.println("Match: " + file.getPath());
		}
	    } else {
		String exp = env.expand(path);
		if (!path.equals(exp)) {
		    System.out.println(path + " -> " + exp);
		} else {
		    System.out.println(path);
		}
		IFile f = fs.getFile(exp);
		if (f.isLink()) {
		    System.out.println(path + " is a link to " + f.getCanonicalPath());
		}
		if (f.isDirectory()) {
		    String[] children = f.list();
		    for (int i=0; i < children.length; i++) {
			System.out.println(children[i]);
		    }
		} else if (f.isFile()) {
		    in = f.getInputStream();
		    String cs = Checksum.getChecksum(in, Checksum.Algorithm.MD5);
		    in.close();
		    System.out.println("Path:  " + path);
		    System.out.println(" Size: " + f.length());
		    System.out.println("  md5: " + cs);
		}
		System.out.println("Canonical path: " + f.getCanonicalPath());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (in != null) {
		    in.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
