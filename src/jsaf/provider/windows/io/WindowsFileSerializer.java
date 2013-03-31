// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jdbm.helper.Serializer;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.windows.identity.IUser;
import jsaf.intf.windows.io.IWindowsFileInfo;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.provider.windows.identity.User;
import jsaf.io.fs.AbstractFilesystem;

/**
 * JDBM Serilizer implementation for Windows IFiles
 */
public class WindowsFileSerializer implements Serializer, Serializable {
    static final int SER_FILE = 0;
    static final int SER_DIRECTORY = 1;
    static final int SER_LINK = 2;

    private transient AbstractFilesystem fs;

    /**
     * The serializer relies on an active IFilesystem, which cannot be serialized, so we serialize the hashcode
     * of the IFilesystem, and maintain a static Map in the parent class. 
     */
    public WindowsFileSerializer(AbstractFilesystem fs) {
	this.fs = fs;
    }

    // Implement Serializer

    public Object deserialize(byte[] serialized) throws IOException {
	DataInput in = new DataInputStream(new ByteArrayInputStream(serialized));
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
	String accountName = in.readUTF();
	String sid = in.readUTF();
	Map<String, String> pe = null;
	int numHeaders = in.readInt();
	if (numHeaders > 0) {
	    pe = new HashMap<String, String>();
	    for (int i=0; i < numHeaders; i++) {
		pe.put(in.readUTF(), in.readUTF());
	    }
	}
	IUser owner = new User((IWindowsSession)fs.getSession(), accountName, sid);
	WindowsFileInfo info = new WindowsFileInfo(type, path, canonicalPath, ctime, mtime, atime, len, winType, owner, pe);
	return fs.createFileFromInfo(info);
    }

    public byte[] serialize(Object obj) throws IOException {
	ByteArrayOutputStream buff = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(buff);
	IFile f = (IFile)obj;
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
	out.writeUTF(info.getOwner().getNetbiosName());
	out.writeUTF(info.getOwner().getSid());
	Map<String, String> pe = info.getPEHeaders();
	if (pe == null) {
	    out.writeInt(0);
	} else {
	    int size = pe.size();
	    out.writeInt(size);
	    for (Map.Entry<String, String> entry : pe.entrySet()) {
		out.writeUTF(entry.getKey());
		out.writeUTF(entry.getValue());
	    }
	}
	out.close();
	return buff.toByteArray();
    }
}
