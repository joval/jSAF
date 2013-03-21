// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.jdbm.Serializer;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.windows.io.IWindowsFileInfo;
import jsaf.io.fs.AbstractFilesystem;

/**
 * JDBM Serilizer implementation for Windows IFiles
 */
public class WindowsFileSerializer implements Serializer<IFile>, Serializable {
    static final int SER_FILE = 0;
    static final int SER_DIRECTORY = 1;
    static final int SER_LINK = 2;

    private Integer instanceKey;
    private transient AbstractFilesystem fs;

    /**
     * The serializer relies on an active IFilesystem, which cannot be serialized, so we serialize the hashcode
     * of the IFilesystem, and maintain a static Map in the parent class. 
     */
    public WindowsFileSerializer(Integer instanceKey) {
	this.instanceKey = instanceKey;
    }

    // Implement Serializer<IFile>

    public IFile deserialize(DataInput in) throws IOException {
	String path = in.readUTF();
	String canonicalPath = in.readUTF();
	long temp = in.readLong();
	Date ctime = temp == IFile.UNKNOWN_TIME ? null : new Date(temp);
	temp = in.readLong();
	Date mtime = temp == IFile.UNKNOWN_TIME ? null : new Date(temp);
	temp = in.readLong();
	Date atime = temp == IFile.UNKNOWN_TIME ? null : new Date(temp);
	IFileMetadata.Type type = IFileMetadata.Type.FILE;
	switch(in.readInt()) {
	  case SER_DIRECTORY:
	    type = IFileMetadata.Type.DIRECTORY;
	    break;
	  case SER_LINK:
	    type = IFileMetadata.Type.LINK;
	    break;
	}
	long len = in.readLong();
	int winType = in.readInt();
	Map<String, String> peHeaders = null;
	int numHeaders = in.readInt();
	if (numHeaders > 0) {
	    peHeaders = new HashMap<String, String>();
	    for (int i=0; i < numHeaders; i++) {
		peHeaders.put(in.readUTF(), in.readUTF());
	    }
	}
	WindowsFileInfo info = new WindowsFileInfo(type, path, canonicalPath, ctime, mtime, atime, len, winType, peHeaders);
	if (fs == null) {
	    fs = AbstractFilesystem.instances.get(instanceKey);
	}
	return fs.createFileFromInfo(info);
    }

    public void serialize(DataOutput out, IFile f) throws IOException {
	out.writeUTF(f.getPath());
	out.writeUTF(f.getCanonicalPath());
	out.writeLong(f.createTime());
	out.writeLong(f.lastModified());
	out.writeLong(f.accessTime());
	if (f.isLink()) {
	    out.writeInt(SER_LINK);
	} else if (f.isDirectory()) {
	    out.writeInt(SER_DIRECTORY);
	} else {
	    out.writeInt(SER_FILE);
	}
	out.writeLong(f.length());
	IWindowsFileInfo info = (IWindowsFileInfo)f.getExtended();
	out.writeInt(info.getWindowsFileType());
	Map<String, String> peHeaders = info.getPEHeaders();
	if (peHeaders == null) {
	    out.writeInt(0);
	} else {
	    int size = peHeaders.size();
	    out.writeInt(size);
	    for (Map.Entry<String, String> entry : peHeaders.entrySet()) {
		out.writeUTF(entry.getKey());
		out.writeUTF(entry.getValue());
	    }
	}
    }
}
