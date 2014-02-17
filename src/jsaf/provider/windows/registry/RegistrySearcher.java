// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jsaf.Message;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.registry.IValue;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.util.StringTools;

/**
 * A class for searching a Windows registry.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class RegistrySearcher implements ISearchable<IKey> {
    private Registry registry;
    private HashSet<String> runspaceIds;
    private Map<String, List<String>> searchMap;

    public RegistrySearcher(Registry registry) throws Exception {
	this.registry = registry;
	runspaceIds = new HashSet<String>();
	searchMap = new HashMap<String, List<String>>();
    }

    // Implement ISearchable<IKey>
 
    public ISearchable.ICondition condition(int field, int type, Object value) {
	return new GenericCondition(field, type, value);
    }

    public List<IKey> search(List<ISearchable.ICondition> conditions) throws Exception {
	IRegistry.Hive hive = null;
	String keyPath = null, fullKeyPath = null, valName = null, valNameB64 = null;
	Pattern keyPattern = null, valPattern = null;
	int maxDepth = DEPTH_UNLIMITED;
	boolean keyOnly = false;
	for (ISearchable.ICondition condition : conditions) {
	    switch(condition.getField()) {
	      case FIELD_DEPTH:
		maxDepth = ((Integer)condition.getValue()).intValue();
		break;
	      case FIELD_FROM:
		fullKeyPath = (String)condition.getValue();
		break;
	      case IRegistry.FIELD_HIVE:
		hive = (IRegistry.Hive)condition.getValue();
		break;
	      case IRegistry.FIELD_KEY:
		switch(condition.getType()) {
		  case TYPE_EQUALITY:
		    keyPath = (String)condition.getValue();
		    break;
		  case TYPE_PATTERN:
		    keyPattern = (Pattern)condition.getValue();
		    break;
		}
		break;
	      case IRegistry.FIELD_VALUE:
		switch(condition.getType()) {
		  case TYPE_EQUALITY:
		    valName = (String)condition.getValue();
		    break;
		  case TYPE_PATTERN:
		    valPattern = (Pattern)condition.getValue();
		    break;
		}
		break;
	      case IRegistry.FIELD_VALUE_BASE64:
		switch(condition.getType()) {
		  case TYPE_EQUALITY:
		    valNameB64 = (String)condition.getValue();
		    break;
		}
		break;
	    }
	}

	String[] keys = null;
	if (fullKeyPath == null) {
	    if (hive == null) {
		throw new IllegalArgumentException("Required search condition FIELD_HIVE is missing");
	    }
	    if (keyPath == null) {
		if (keyPattern != null) {
	 	    keys = guessParent(keyPattern, hive);
		}
		if (keys == null) {
		    keys = new String[]{null};
		}
	    } else {
		keys = new String[]{keyPath};
	    }
	} else {
	    IKey key = registry.getKey(fullKeyPath);
	    hive = key.getHive();
	    keys = new String[]{key.getPath()};
	}

	List<IKey> results = new ArrayList<IKey>();
	for (String from : keys) {
	    StringBuffer sb = new StringBuffer("Find-RegKeys -Hive \"").append(hive).append("\"");
	    if (from != null) {
		sb.append(" -Key \"").append(from).append("\"");
	    }
	    if (keyPattern != null) {
		sb.append(" -Pattern \"").append(toString(keyPattern)).append("\"");
	    }
	    if (valName != null) {
		sb.append(" -WithLiteralVal \"").append(valName).append("\"");
	    } else if (valNameB64 != null) {
		sb.append(" -WithEncodedVal ").append(valNameB64);
	    } else if (valPattern != null) {
		sb.append(" -WithValPattern \"").append(toString(valPattern)).append("\"");
	    }
	    sb.append(" -Depth ").append(Integer.toString(maxDepth));
	    sb.append(" | %{$_.Name}");

	    String command = sb.toString();
	    if (searchMap.containsKey(command)) {
		for (String fullPath : searchMap.get(command)) {
		    results.add(registry.getKey(fullPath));
		}
	    } else {
		String paths = getRunspace().invoke(command, registry.session.getTimeout(IWindowsSession.Timeout.XL));
		if (paths == null) {
		    searchMap.put(command, new ArrayList<String>());
		} else {
		    List<String> result = new ArrayList<String>();
		    searchMap.put(command, result);
		    for (String fullPath : paths.split("\r\n")) {
			result.add(fullPath);
			results.add(registry.getKey(fullPath));
		    }
		}
	    }
	}
	return results;
    }

    /**
     * Return a list of Key paths containing potential matches for the specified pattern.
     */
    public String[] guessParent(Pattern p, Object... args) {
	IRegistry.Hive hive = null;
	for (Object arg : args) {
	    if (arg instanceof IRegistry.Hive) {
		hive = (IRegistry.Hive)arg;
		break;
	    }
	}

	String path = toString(p);
	if (!path.startsWith("^")) {
	    return null;
	}
	path = path.substring(1);

	int ptr = path.indexOf(IRegistry.ESCAPED_DELIM);
	if (ptr == -1) {
	    return Arrays.asList(path).toArray(new String[1]);
	}

	StringBuffer sb = new StringBuffer(path.substring(0,ptr));
	ptr += IRegistry.ESCAPED_DELIM.length();
	int next = ptr;
	while((next = path.indexOf(IRegistry.ESCAPED_DELIM, ptr)) != -1) {
	    String token = path.substring(ptr, next);
	    if (StringTools.containsRegex(token)) {
		break;
	    } else {
		sb.append(IRegistry.DELIM_STR).append(token);
		ptr = next + IRegistry.ESCAPED_DELIM.length();
	    }
	}
	if (sb.length() == 0) {
	    return null;
	} else {
	    String parent = sb.toString();

	    // One of the children of parent should match...
	    StringBuffer prefix = new StringBuffer("^");
	    String token = path.substring(ptr);
	    for (int i=0; i < token.length(); i++) {
		char c = token.charAt(i);
		boolean isRegexChar = false;
		for (char ch : StringTools.REGEX_CHARS) {
		    if (c == ch) {
			isRegexChar = true;
			break;
		    }
		}
		if (isRegexChar) {
		    break;
		} else {
		    prefix.append(c);
		}
	    }
	    try {
		IKey base = registry.getKey(hive, parent);
		if (prefix.length() > 1) {
		    ArrayList<String> paths = new ArrayList<String>();
		    for (String subkeyName : base.listSubkeys(Pattern.compile(prefix.toString()))) {
			paths.add(base.getPath() + IRegistry.DELIM_STR + subkeyName);
		    }
		    return paths.toArray(new String[paths.size()]);
		}
	    } catch (NoSuchElementException e) {
		return new String[0];
	    } catch (Exception e) {
		registry.getLogger().warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }

	    return Arrays.asList(parent).toArray(new String[1]);
	}
    }

    // Internal

    String toString(Pattern p) {
	return StringTools.regexPosix2Powershell(p.pattern());
    }

    // Private

    private IRunspace getRunspace() throws Exception {
	IRunspace runspace = registry.getRunspace();
	if (!runspaceIds.contains(runspace.getId())) {
	    runspace.loadModule(getClass().getResourceAsStream("RegistrySearcher.psm1"));
	    runspaceIds.add(runspace.getId());
	}
	return runspace;
    }
}
