// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.cal10n.LocLogger;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;
import jdbm.helper.Serializer;
import jdbm.helper.StringComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

import jsaf.Message;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileEx;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.io.IRandomAccess;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.IProperty;
import jsaf.intf.util.ISearchable;
import jsaf.intf.system.ISession;
import jsaf.intf.system.IEnvironment;
import jsaf.util.StringTools;

/**
 * An abstract IFilesystem implementation with caching and convenience methods.
 *
 * All IFilesystem implementations extend this base class.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public abstract class AbstractFilesystem implements IFilesystem {
    protected boolean autoExpand = true;
    protected IProperty props;
    protected ISession session;
    protected IEnvironment env;
    protected LocLogger logger;

    protected final String ESCAPED_DELIM;
    protected final String DELIM;

    protected AbstractFilesystem(ISession session, String delim, String dbkey) {
	ESCAPED_DELIM = Matcher.quoteReplacement(delim);
	DELIM = delim;
	this.session = session;
	logger = session.getLogger();
	props = session.getProperties();
	env = session.getEnvironment();

	if (session.getProperties().getBooleanProperty(IFilesystem.PROP_CACHE_JDBM)) {
	    try {
		cache = new JDBMCache(dbkey);
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		cache = new HashMap<String, IFile>();
	    }
	} else {
	    cache = new HashMap<String, IFile>();
	}
    }

    public void setAutoExpand(boolean autoExpand) {
	this.autoExpand = autoExpand;
    }

    public ISession getSession() {
	return session;
    }

    public void dispose() {
	if (cache instanceof JDBMCache) {
	    try {
		((JDBMCache)cache).dispose();
	    } catch (Exception e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}
	cache = null;
    }

    /**
     * For use by the ISearchable.
     *
     * @param file indicates if the pattern represents a file (or if false, a directory).
     *
     * @return null if a guess cannot be made (e.g., not a rooted pattern), an empty array if the pattern
     *         cannot possibly yield any search results, or an array of potential matching parent directories
     *         beneath which a file matching the pattern could reside.
     */
    public String[] guessParent(Pattern p, boolean file) {
	String path = p.pattern();
	if (!path.startsWith("^")) {
	    return null;
	} else if (session instanceof IUnixSession) {
	    if (!path.startsWith("^/")) {
		// All valid Unix paths must start with forward slash
		return new String[0];
	    }
	}

	path = path.substring(1);

	int ptr = path.indexOf(ESCAPED_DELIM);
	if (ptr == -1) {
	    if (StringTools.containsRegex(path)) {
		// give up
		return null;
	    } else {
		return Arrays.asList(path).toArray(new String[1]);
	    }
	}

	String token = path.substring(0, ptr);
	if (StringTools.containsRegex(token)) {
	    // give up
	    return null;
	}

	StringBuffer sb = new StringBuffer(token);
	int next = ptr;
	while((next = path.indexOf(ESCAPED_DELIM, ptr)) != -1) {
	    token = path.substring(ptr, next);
	    if (StringTools.containsRegex(token)) {
		break;
	    } else {
		if (!sb.toString().endsWith(DELIM)) {
		    sb.append(DELIM);
		}
		sb.append(token);
		ptr = next + ESCAPED_DELIM.length();
	    }
	}
	if (sb.length() == 0) {
	    return null;
	} else {
	    String parent = sb.toString();

	    // One of the children of parent should match...
	    StringBuffer prefix = new StringBuffer("^");
	    token = path.substring(ptr);
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
		if (!file && prefix.length() > 1) {
		    IFile base = getFile(parent);
		    if (base.exists()) {
			List<String> candidates = new ArrayList<String>();
			Pattern pattern = null;
			switch(session.getType()) {
			  case WINDOWS:
			    pattern = Pattern.compile(prefix.toString(), Pattern.CASE_INSENSITIVE);
			    break;
			  default:
			    pattern = Pattern.compile(prefix.toString());
			    break;
			}
			for (IFile f : base.listFiles(pattern)) {
			    if (f.isDirectory()) {
				candidates.add(f.getPath());
			    }
			}
			return candidates.toArray(new String[candidates.size()]);
		    } else {
			return new String[0];
		    }
		}
	    } catch (Exception e) {
	    }

	    return Arrays.asList(parent).toArray(new String[1]);
	}
    }

    /**
     * Return an implementation-specific IFile serializer for JDBM.
     */
    public abstract Serializer getFileSerializer(AbstractFilesystem fs);

    /**
     * Return an implementation-specific IFile instance based on an IAccessor. The AbstractFilesystem base class invokes
     * this method if it cannot return the file from the cache.
     */
    protected abstract IFile getPlatformFile(String path, IFile.Flags flags) throws IllegalArgumentException, IOException;

    /**
     * For use during deserialization.  A serialized IFile will only consist of metadata.  So, to reconstitute an IFile
     * capable of accessing the underlying file, use:
     *
     * <code>AbstractFilesystem.instances.get(instanceKey).createFileFromInfo(...)</code>
     */
    public IFile createFileFromInfo(IFileMetadata info) {
	IFile f = new DefaultFile(info, IFile.Flags.READONLY);
	return f;
    }

    // Implement ILoggable

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    public LocLogger getLogger() {
	return logger;
    }

    // Implement IFilesystem (partially)

    public Collection<IMount> getMounts() throws IOException {
	return getMounts(null);
    }

    public Collection<IMount> getMounts(Pattern typeFilter) throws IOException {
	return getMounts(typeFilter, false);
    }

    public String getDelimiter() {
	return DELIM;
    }

    public final IFile getFile(String path) throws IOException {
	return getFile(path, IFile.Flags.READONLY);
    }

    public IFile[] getFiles(String[] paths) throws IOException {
	IFile[] files = new IFile[paths.length];
	for (int i=0; i < paths.length; i++) {
	    files[i] = getFile(paths[i], IFile.Flags.READONLY);
	}
	return files;
    }

    public final IFile getFile(String path, IFile.Flags flags) throws IOException {
	if (autoExpand) {
	    path = env.expand(path);
	}
	switch(flags) {
	  case READONLY:
	    try {
		return getCache(path);
	    } catch (NoSuchElementException e) {
	    }
	    // fall-thru

	  default:
	    return getPlatformFile(path, flags);
	}
    }

    public final IRandomAccess getRandomAccess(IFile file, String mode) throws IllegalArgumentException, IOException {
	return file.getRandomAccess(mode);
    }

    public final IRandomAccess getRandomAccess(String path, String mode) throws IllegalArgumentException, IOException {
	IFile.Flags flags = "rw".equals(mode) ? flags = IFile.Flags.READWRITE : IFile.Flags.READONLY;
	return getFile(path, flags).getRandomAccess(mode);
    }

    public final InputStream getInputStream(String path) throws IOException {
	return getFile(path).getInputStream();
    }

    public final OutputStream getOutputStream(String path, boolean append) throws IOException {
	return getFile(path, IFile.Flags.READWRITE).getOutputStream(append);
    }

    // Inner Classes

    /**
     * A JDBM-backed implementation of the cache Map.
     */
    public class JDBMCache implements Map<String, IFile> {
	private RecordManager recman;
	private String dbkey;
	private BTree tree;
	private BTree index;
	private int writes = 0;

	JDBMCache(String dbkey) throws IOException {
	    this.dbkey = dbkey;
	    cleanFiles();
	    String basename = new File(session.getWorkspace(), dbkey).toString();
	    Properties props = new Properties();
	    props.setProperty(RecordManagerOptions.CACHE_TYPE, RecordManagerOptions.NORMAL_CACHE);
	    props.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "true");
	    recman = RecordManagerFactory.createRecordManager(basename, props);
	    tree = BTree.createInstance(recman, new StringComparator(), null, getFileSerializer(AbstractFilesystem.this));
	    index = BTree.createInstance(recman, new StringComparator());
	}

	void dispose() throws IOException {
	    recman.delete(tree.getRecid());
	    recman.commit();
	    recman.close();
	    cleanFiles();
	}

	// Implement Map

	public boolean containsKey(Object key) {
	    try {
		//
		// Using a separate tree for the index prevents a deserialization infinite loop.
		//
		return index.find(key) != null;
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return false;
	}

	public boolean containsValue(Object value) {
	    try {
		Tuple t = new Tuple();
		TupleBrowser iter = tree.browse();
		while(iter.getNext(t)) {
		    if (t.getValue().equals(value)) {
			return true;
		    }
		}
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return false;
	}

	public IFile get(Object key) {
	    try {
		return (IFile)tree.find(key);
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return null;
	}

	public boolean isEmpty() {
	    return size() == 0;
	}

	public IFile put(String key, IFile value) {
	    try {
		IFile f = (IFile)tree.insert(key, value, true);
		index.insert(key, "", false);
		wrote();
		return f;
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return null;
	}

	public void putAll(Map<? extends String, ? extends IFile> m) {
	    try {
		for (Map.Entry<? extends String, ? extends IFile> entry : m.entrySet()) {
		    tree.insert(entry.getKey(), entry.getValue(), true);
		    index.insert(entry.getKey(), "", true);
		}
		wrote();
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}

	public IFile remove(Object key) {
	    try {
		IFile f = (IFile)tree.remove(key);
		index.remove(key);
		wrote();
		return f;
	    } catch (IOException e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	    return null;
	}

	public int size() {
	    return index.size();
	}

	public void clear() {
	    throw new UnsupportedOperationException();
	}

	public Set<Map.Entry<String, IFile>> entrySet() {
	    throw new UnsupportedOperationException();
	}

	public Set<String> keySet() {
	    throw new UnsupportedOperationException();
	}

	public Collection<IFile> values() {
	    throw new UnsupportedOperationException();
	}

	// Private

	private void cleanFiles() throws IOException {
	    for (File f : session.getWorkspace().listFiles()) {
		if (f.getName().startsWith(dbkey)) {
		    if (!f.delete()) {
			throw new IOException("Failed to delete " + f.toString());
		    }
		}
	    }
	}

	/**
	 * Register performance of a write operation to the cache.  This triggers the occasional commit to disk.
	 */
	private void wrote() throws IOException {
	    if (++writes % 10000 == 0) {
		recman.commit();
		writes = 0;
	    }
	}
    }

    /**
     * The default implementation of an IFile -- works with Java (local) Files.
     */
    public class DefaultFile implements IFile {
	protected String path;
	protected IAccessor accessor;
	protected IFileMetadata info;
	protected Flags flags;

	/**
	 * Create a file from an accessor.
	 */
	public DefaultFile(String path, IAccessor accessor, Flags flags) {
	    this.path = path;
	    this.accessor = accessor;
	    this.flags = flags;
	}

	/**
	 * Create a file from metadata. Note that subclasses must ALWAYS delegate to this constructor when initializing
	 * from IFileMetadata, or else the metadata will not be cached.
	 */
	public DefaultFile(IFileMetadata info, Flags flags) {
	    path = info.getPath();
	    this.info = info;
	    this.flags = flags;

	    //
	    // Place in the cache if READONLY
	    //
	    if (flags == IFile.Flags.READONLY) {
		try {
		    putCache(this);
		} catch (Exception e) {
		    logger.error("Exception caching entry for " + toString());
		    logger.error(Message.getMessage(Message.ERROR_EXCEPTION), e);
		}
	    }
	}

	@Override
	public String toString() {
	    return path;
	}

	// Implement IFileMetadata

	public Type getType() throws IOException {
	    return getInfo().getType();
	}

	public String getLinkPath() throws IllegalStateException, IOException {
	    return getInfo().getLinkPath();
	}

	public Date getAccessTime() throws IOException {
	    return getInfo().getAccessTime();
	}

	public long accessTime() throws IOException {
	    return getInfo().accessTime();
	}

	public Date getCreateTime() throws IOException {
	    return getInfo().getCreateTime();
	}

	public long createTime() throws IOException {
	    return getInfo().createTime();
	}

	public Date getLastModified() throws IOException {
	    if (info == null) {
		return accessor.getMtime();
	    } else {
		return info.getLastModified();
	    }
	}

	public long lastModified() throws IOException {
	    if (info == null) {
		return accessor.getMtime() == null ? IFile.UNKNOWN_TIME : accessor.getMtime().getTime();
	    } else {
		return info.lastModified();
	    }
	}

	public long length() throws IOException {
	    if (info == null) {
		return accessor.getLength();
	    } else {
		return info.length();
	    }
	}

	public String getPath() {
	    return path;
	}

	public String getCanonicalPath() throws IOException {
	    if (info == null) {
		return accessor.getCanonicalPath();
	    } else {
		return info.getCanonicalPath();
	    }
	}

	public IFileEx getExtended() throws IOException {
	    return getInfo().getExtended();
	}

	// Implement IFile

	public String getName() {
	    if (path.equals(DELIM)) {
		return path;
	    } else {
		int ptr = path.lastIndexOf(DELIM);
		if (ptr == -1) {
		    return path;
		} else {
		    return path.substring(ptr + DELIM.length());
		}
	    }
	}

	public String getParent() {
	    if (path.equals(DELIM)) {
		return path;
	    } else {
		int ptr = path.lastIndexOf(DELIM);
		if (ptr == -1) {
		    return path;
		} else {
		    return path.substring(0, ptr);
		}
	    }
	}

	public boolean isDirectory() throws IOException {
	    try {
		return getInfo().getType() == Type.DIRECTORY;
	    } catch (FileNotFoundException e) {
		return false;
	    }
	}

	public boolean isFile() throws IOException {
	    try {
		return getInfo().getType() == Type.FILE;
	    } catch (FileNotFoundException e) {
		return false;
	    }
	}

	public boolean isLink() throws IOException {
	    try {
		return getInfo().getType() == Type.LINK;
	    } catch (FileNotFoundException e) {
		return false;
	    }
	}

	public boolean exists() {
	    if (info == null) {
		try {
		    return accessor.exists();
		} catch (Exception e) {
		    return false;
		}
	    } else {
		return true;
	    }
	}

	public final boolean mkdir() {
	    switch(flags) {
	      case READWRITE:
		try {
		    return getAccessor().mkdir();
		} catch (IOException e) {
		    return false;
		}
	      default:
		return false;
	    }
	}

	public InputStream getInputStream() throws IOException {
	    return getAccessor().getInputStream();
	}

	public final OutputStream getOutputStream(boolean append) throws IOException {
	    switch(flags) {
	      case READWRITE:
		return getAccessor().getOutputStream(append);
	      default:
		throw new AccessControlException("Method: getOutputStream, Flags: " + flags);
	    }
	}

	public final IRandomAccess getRandomAccess(String mode) throws IllegalArgumentException, IOException {
	    if ("rw".equals(mode) && flags != IFile.Flags.READWRITE) {
		throw new AccessControlException("Method: getRandomAccess, Mode: " + mode + ", Flags: " + flags);
	    }
	    return getAccessor().getRandomAccess(mode);
	}

	public String[] list() throws IOException {
	    if (isDirectory()) {
		String[] list = getAccessor().list();
		if (list == null) {
		    return new String[0];
		} else {
		    return list;
		}
	    } else {
		return null;
	    }
	}

	public IFile[] listFiles() throws IOException {
	    return listFiles(null);
	}

	public IFile[] listFiles(Pattern p) throws IOException {
	    String[] fnames = list();
	    if (fnames == null) {
		return null;
	    }
	    List<IFile> result = new ArrayList<IFile>();
	    for (String fname : fnames) {
		if (p == null || p.matcher(fname).find()) {
		    if (path.endsWith(DELIM)) {
			result.add(getFile(path + fname, flags));
		    } else {
			result.add(getFile(path + DELIM + fname, flags));
		    }
		}
	    }
	    return result.toArray(new IFile[result.size()]);
	}

	public IFile getChild(String name) throws IOException {
	    if (isDirectory()) {
		return getFile(path + DELIM + name, flags);
	    } else {
		throw new IOException("Not a directory: " + path);
	    }
	}

	public void delete() throws IOException {
	    switch(flags) {
	      case READWRITE:
		getAccessor().delete();
		break;
	      default:
		throw new AccessControlException("Method: delete, Flags: " + flags);
	    }
	}

	// Internal

	protected IAccessor getAccessor() throws IOException {
	    if (accessor == null) {
		// Info must not be null
		accessor = new DefaultAccessor(new File(info.getPath()));
	    }
	    return accessor;
	}

	protected final IFileMetadata getInfo() throws IOException {
	    if (info == null) {
		// Accessor must not be null
		info = getAccessor().getInfo();

		path = info.getPath();

		//
		// Now that we have info, cache it if this is a READONLY IFile
		//
		if (flags == IFile.Flags.READONLY) {
		    putCache(this);
		}
	    }
	    return info;
	}
    }

    /**
     * A FileAccessor implementation for Java (local) Files.
     */
    public class DefaultAccessor implements IAccessor {
	protected File file;

	protected DefaultAccessor(File file) {
	    this.file = file;
	}

	public String toString() {
	    return file.toString();
	}

	// Implement IAccessor

	public boolean exists() {
	    return file.exists();
	}

	public void delete() {
	    file.delete();
	}

	public DefaultMetadata getInfo() throws IOException {
	    if (exists()) {
		// Determine the file type...

		IFileMetadata.Type type = IFileMetadata.Type.FILE;
		String path = file.getPath();
		String canonicalPath = file.getCanonicalPath();
		String linkPath = null;

		File canon;
		if (file.getParent() == null) {
		    canon = file;
		} else {
		    File canonDir = file.getParentFile().getCanonicalFile();
		    canon = new File(canonDir, file.getName());
		}

		if (!canon.getCanonicalFile().equals(canon.getAbsoluteFile())) {
		    type = IFileMetadata.Type.LINK;
		    linkPath = canonicalPath;
		} else if (file.isDirectory()) {
		    type = IFileMetadata.Type.DIRECTORY;
		}

		return new DefaultMetadata(type, path, linkPath, canonicalPath, this);
	    } else {
		throw new FileNotFoundException(file.getPath());
	    }
	}

	public Date getCtime() throws IOException {
	    return null;
	}

	public Date getAtime() throws IOException {
	    return null;
	}

	public Date getMtime() throws IOException {
	    return new Date(file.lastModified());
	}

	public long getLength() throws IOException {
	    return file.length();
	}

	public IRandomAccess getRandomAccess(String mode) throws IOException {
	    return new RandomAccessImpl(new RandomAccessFile(file, mode));
	}

	public InputStream getInputStream() throws IOException {
	    return new FileInputStream(file);
	}

	public OutputStream getOutputStream(boolean append) throws IOException {
	    return new FileOutputStream(file, append);
	}

	public String getCanonicalPath() throws IOException {
	    return file.getCanonicalPath();
	}

	public String[] list() throws IOException {
	    return file.list();
	}

	public boolean mkdir() {
	    return file.mkdir();
	}

	// Internal

	/**
	 * Default implementation of IRandomAccess
	 */
	class RandomAccessImpl implements IRandomAccess {
	    private RandomAccessFile raf;

	    public RandomAccessImpl(RandomAccessFile raf) {
		this.raf = raf;
	    }

	    // Implement IRandomAccess

	    public void readFully(byte[] buff) throws IOException {
		raf.readFully(buff);
	    }

	    public void close() throws IOException {
		raf.close();
	    }

	    public void seek(long pos) throws IOException {
		raf.seek(pos);
	    }

	    public int read() throws IOException {
		return raf.read();
	    }

	    public int read(byte[] buff) throws IOException {
		return raf.read(buff);
	    }

	    public int read(byte[] buff, int offset, int len) throws IOException {
		return raf.read(buff, offset, len);
	    }

	    public long length() throws IOException {
		return raf.length();
	    }

	    public long getFilePointer() throws IOException {
		return raf.getFilePointer();
	    }
	}
    }

    // Private

    private Map<String, IFile> cache;

    /**
     * Attempt to retrieve an IFile from the cache.
     *
     * TBD: expire objects that get too old
     */
    private IFile getCache(String path) throws NoSuchElementException {
	IFile f = cache.get(path);
	if (f == null) {
	    throw new NoSuchElementException(path);
	} else {
	    logger.trace(Message.STATUS_FS_CACHE_RETRIEVE, path);
	    return f;
	}
    }

    /**
     * Put an IFile in the cache.
     *
     */
    private void putCache(IFile file) {
	String path = file.getPath();

	//
	// TBD: see if the data is newer than what's already in the cache?
	//
	if (!cache.containsKey(path)) {
	    logger.trace(Message.STATUS_FS_CACHE_STORE, path);
	    cache.put(path, file);
	}
    }
}
