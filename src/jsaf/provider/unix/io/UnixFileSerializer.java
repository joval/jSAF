// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

import jdbm.helper.Serializer;

import jsaf.intf.io.IFile;
import jsaf.intf.io.IFileMetadata;
import jsaf.intf.unix.io.IUnixFileInfo;
import jsaf.io.fs.AbstractFilesystem;

/**
 * JDBM Serilizer implementation for Unix IFiles
 */
public class UnixFileSerializer implements Serializer, Serializable {
    static final long UNKNOWN_TIME = -1;

    static final int SER_FILE = 0;
    static final int SER_DIRECTORY = 1;
    static final int SER_LINK = 2;

    private transient AbstractFilesystem fs;

    /**
     * The serializer relies on an active IFilesystem, which cannot be serialized, so we serialize the hashcode
     * of the IFilesystem, and maintain a static Map in the parent class. 
     */
    public UnixFileSerializer(AbstractFilesystem fs) {
	this.fs = fs;
    }

    // Implement Serializer

    public Object deserialize(byte[] serialized) throws IOException {
	DataInput in = new DataInputStream(new ByteArrayInputStream(serialized));
	String path = in.readUTF();
	String link = null;
	long temp = in.readLong();
	Date ctime = temp == UNKNOWN_TIME ? null : new Date(temp);
	temp = in.readLong();
	Date mtime = temp == UNKNOWN_TIME ? null : new Date(temp);
	temp = in.readLong();
	Date atime = temp == UNKNOWN_TIME ? null : new Date(temp);
	IFileMetadata.Type type = IFileMetadata.Type.FILE;
	switch(in.readInt()) {
	  case SER_DIRECTORY:
	    type = IFileMetadata.Type.DIRECTORY;
	    break;
	  case SER_LINK:
	    type = IFileMetadata.Type.LINK;
	    link = in.readUTF();
	    break;
	}
	long len = in.readLong();
	char uType = in.readChar();
	String perms = in.readUTF();
	int uid = in.readInt();
	int gid = in.readInt();
	Boolean hasAcl = null;
	switch(in.readShort()) {
	  case 0:
	    hasAcl = Boolean.FALSE;
	    break;
	  case 1:
	    hasAcl = Boolean.FALSE;
	    break;
	}
	Properties ext = null;
	if (in.readBoolean()) {
	    ext = new Properties();
	    int propertyCount = in.readInt();
	    for (int i=0; i < propertyCount; i++) {
		ext.setProperty(in.readUTF(), in.readUTF());
	    }
	}
	UnixFileInfo info = new UnixFileInfo(type, path, link, ctime, mtime, atime, len, uType, perms, uid, gid, hasAcl, ext);
	return fs.createFileFromInfo(info);
    }

    public byte[] serialize(Object obj) throws IOException {
	ByteArrayOutputStream buff = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(buff);
	IFile f = (IFile)obj;
	out.writeUTF(f.getPath());
	out.writeLong(f.getCreateTime() == null ? UNKNOWN_TIME : f.getCreateTime().getTime());
	out.writeLong(f.getLastModified() == null ? UNKNOWN_TIME : f.getLastModified().getTime());
	out.writeLong(f.getAccessTime() == null ? UNKNOWN_TIME : f.getAccessTime().getTime());
	IUnixFileInfo info = (IUnixFileInfo)f.getExtended();
	if (f.isLink()) {
	    out.writeInt(SER_LINK);
	    String s = f.getLinkPath();
	    out.writeUTF(s == null ? "" : s);
	} else if (f.isDirectory()) {
	    out.writeInt(SER_DIRECTORY);
	} else {
	    out.writeInt(SER_FILE);
	}
	out.writeLong(f.length());

	String uType = info.getUnixFileType();
	if (IUnixFileInfo.FILE_TYPE_DIR.equals(uType)) {
	    out.writeChar(IUnixFileInfo.DIR_TYPE);
	} else if (IUnixFileInfo.FILE_TYPE_FIFO.equals(uType)) {
	    out.writeChar(IUnixFileInfo.FIFO_TYPE);
	} else if (IUnixFileInfo.FILE_TYPE_LINK.equals(uType)) {
	    out.writeChar(IUnixFileInfo.LINK_TYPE);
	} else if (IUnixFileInfo.FILE_TYPE_BLOCK.equals(uType)) {
	    out.writeChar(IUnixFileInfo.BLOCK_TYPE);
	} else if (IUnixFileInfo.FILE_TYPE_CHAR.equals(uType)) {
	    out.writeChar(IUnixFileInfo.CHAR_TYPE);
	} else if (IUnixFileInfo.FILE_TYPE_SOCK.equals(uType)) {
	    out.writeChar(IUnixFileInfo.SOCK_TYPE);
	} else {
	    out.writeChar(IUnixFileInfo.FILE_TYPE);
	}

	out.writeUTF(info.getPermissions());
	out.writeInt(info.getUserId());
	out.writeInt(info.getGroupId());
	Boolean hasAcl = info.hasPosixAcl();
	if (hasAcl == null) {
	    out.writeShort(2);
	} else if (Boolean.TRUE.equals(hasAcl)) {
	    out.writeShort(1);
	} else {
	    out.writeShort(0);
	}
	String[] extendedKeys = info.getExtendedKeys();
	if (extendedKeys == null) {
	    out.writeBoolean(false);
	} else {
	    out.writeBoolean(true);
	    out.writeInt(extendedKeys.length);
	    for (int i=0; i < extendedKeys.length; i++) {
		out.writeUTF(extendedKeys[i]);
		out.writeUTF(info.getExtendedData(extendedKeys[i]));
	    }
	}
	out.close();
	return buff.toByteArray();
    }
}
