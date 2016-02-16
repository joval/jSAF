// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import jsaf.intf.io.IFileEx;
import jsaf.intf.windows.identity.IUser;

/**
 * Defines extended attributes of a file on Windows.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IWindowsFileInfo extends IFileEx {
    /**
     * An enumeration of Windows file types.
     *
     * @since 1.3
     */
    enum WindowsType {
	FILE_TYPE_UNKNOWN(0x0000),
	FILE_TYPE_DISK(0x0001),
	FILE_TYPE_CHAR(0x0002),
	FILE_TYPE_PIPE(0x0003),
	FILE_TYPE_REMOTE(0x8000);

 	/**
	 * Get the WindowsType corresponding to the specified value.
	 */
	public static WindowsType getWindowsType(int value) {
	    for (WindowsType type : values()) {
		if (value == type.value()) {
		    return type;
		}
	    }
	    throw new IllegalArgumentException(Integer.toString(value));
	}

	private int value;

	private WindowsType(int value) {
	    this.value = value;
	}

	public int value() {
	    return value;
	}
    }

    /**
     * Returns the file's WindowsType, or null if the file is a directory.
     *
     * @since 1.3
     */
    WindowsType getWindowsType() throws IOException;

    /**
     * An enumeration of Windows file attributes.
     * See: https://msdn.microsoft.com/en-us/library/windows/desktop/gg258117(v=vs.85).aspx
     *
     * @since 1.3.4
     */
    enum Attribute {
	/**
	 * A file or directory that is an archive file or directory. Applications typically use this attribute to mark files for
	 * backup or removal . 
	 */
	FILE_ATTRIBUTE_ARCHIVE(32),

	/**
	 * A file or directory that is compressed. For a file, all of the data in the file is compressed. For a directory, 
	 * compression is the default for newly created files and subdirectories.
	 */
	FILE_ATTRIBUTE_COMPRESSED(2048),

	/**
	 * This value is reserved for system use.
	 */
	FILE_ATTRIBUTE_DEVICE(64),

	/**
	 * The handle that identifies a directory.
	 */
	FILE_ATTRIBUTE_DIRECTORY(16),

	/**
	 * A file or directory that is encrypted. For a file, all data streams in the file are encrypted. For a directory,
	 * encryption is the default for newly created files and subdirectories.
	 */
	FILE_ATTRIBUTE_ENCRYPTED(16384),

	/**
	 * The file or directory is hidden. It is not included in an ordinary directory listing.
	 */
	FILE_ATTRIBUTE_HIDDEN(2),

	/**
	 * The directory or user data stream is configured with integrity (only supported on ReFS volumes). It is not
	 * included in an ordinary directory listing. The integrity setting persists with the file if it's renamed.
	 * If a file is copied the destination file will have integrity set if either the source file or destination
	 * directory have integrity set.
	 *
	 * Windows Server 2008 R2, Windows 7, Windows Server 2008, Windows Vista, Windows Server 2003, and Windows XP:
	 * This flag is not supported until Windows Server 2012.
	 */
	FILE_ATTRIBUTE_INTEGRITY_STREAM(32768),

	/**
	 * A file that does not have other attributes set. This attribute is valid only when used alone.
	 */
	FILE_ATTRIBUTE_NORMAL(128),

	/**
	 * The file or directory is not to be indexed by the content indexing service.
	 */
	FILE_ATTRIBUTE_NOT_CONTENT_INDEXED(8192),

	/**
	 * The user data stream not to be read by the background data integrity scanner (AKA scrubber). When set on
	 * a directory it only provides inheritance. This flag is only supported on Storage Spaces and ReFS volumes.
	 * It is not included in an ordinary directory listing.
	 *
	 * Windows Server 2008 R2, Windows 7, Windows Server 2008, Windows Vista, Windows Server 2003, and Windows XP:
	 * This flag is not supported until Windows 8 and Windows Server 2012.
	 */
	FILE_ATTRIBUTE_NO_SCRUB_DATA(131072),

	/**
	 * The data of a file is not available immediately. This attribute indicates that the file data is physically
	 * moved to offline storage. This attribute is used by Remote Storage, which is the hierarchical storage
	 * management software. Applications should not arbitrarily change this attribute.
	 */
	FILE_ATTRIBUTE_OFFLINE(4096),

	/**
	 * A file that is read-only. Applications can read the file, but cannot write to it or delete it. This attribute
	 * is not honored on directories. For more information, see You cannot view or change the Read-only or the
	 * System attributes of folders in Windows Server 2003, in Windows XP, in Windows Vista or in Windows 7.
	 */
	FILE_ATTRIBUTE_READONLY(1),

	/**
	 * A file or directory that has an associated reparse point, or a file that is a symbolic link.
	 */
	FILE_ATTRIBUTE_REPARSE_POINT(1024),

	/**
	 * A file that is a sparse file.
	 */
	FILE_ATTRIBUTE_SPARSE_FILE(512),

	/**
	 * A file or directory that the operating system uses a part of, or uses exclusively.
	 */
	FILE_ATTRIBUTE_SYSTEM(4),

	/**
	 * A file that is being used for temporary storage. File systems avoid writing data back to mass storage if
	 * sufficient cache memory is available, because typically, an application deletes a temporary file after the
	 * handle is closed. In that scenario, the system can entirely avoid writing the data. Otherwise, the data is
	 * written after the handle is closed.
	 */
	FILE_ATTRIBUTE_TEMPORARY(256),

	/**
	 * This value is reserved for system use.
	 */
	FILE_ATTRIBUTE_VIRTUAL(65536);

	/**
	 * Identifies the attributes in the specified int value.
	 */
	static Collection<Attribute> values(int attrs) {
	    Collection<Attribute> result = new ArrayList<Attribute>();
	    for (Attribute attr : values()) {
		if ((attr.val & attrs) == attr.val) {
		    result.add(attr);
		}
	    }
	    return result;
	}

	/**
	 * Converts a collection of attributes back into an int representation.
	 */
	static int intValue(Collection<Attribute> attrs) {
	    int val = 0;
	    for (Attribute attr : attrs) {
		val += attr.val;
	    }
	    return val;
	}

	// Private

	private int val;

	private Attribute(int val) {
	    this.val = val;
	}
    }

    /**
     * Returns the file's attributes.
     *
     * @since 1.3.4
     */
    Collection<Attribute> getAttributes() throws IOException;

    /**
     * @since 1.0.1
     */
    String PE_MS_CHECKSUM = "MSChecksum";

    /**
     * @since 1.0.1
     */
    String PE_VERSION = "FileVersion";

    /**
     * @since 1.0.1
     */
    String PE_VERSION_MAJOR_PART = "FileMajorPart";

    /**
     * @since 1.0.1
     */
    String PE_VERSION_MINOR_PART = "FileMinorPart";

    /**
     * @since 1.0.1
     */
    String PE_VERSION_BUILD_PART = "FileBuildPart";

    /**
     * @since 1.0.1
     */
    String PE_VERSION_PRIVATE_PART = "FilePrivatePart";

    /**
     * @since 1.0.1
     */
    String PE_PRODUCT_NAME = "ProductName";

    /**
     * @since 1.0.1
     */
    String PE_PRODUCT_VERSION = "ProductVersion";

    /**
     * @since 1.0.1
     */
    String PE_COMPANY_NAME = "CompanyName";

    /**
     * @since 1.0.1
     */
    String PE_LANGUAGE = "Language";

    /**
     * @since 1.0.1
     */
    String PE_ORIGINAL_NAME = "OriginalFilename";

    /**
     * @since 1.0.1
     */
    String PE_INTERNAL_NAME = "InternalName";

    /**
     * Get the file owner.
     */
    IUser getOwner() throws IOException;

    /**
     * Retrieve information from the Portable Execution (PE) format file headers.
     *
     * @return null if not a PE-format file
     * @since 1.0.1
     */
    Map<String, String> getPEHeaders() throws IOException;

    /**
     * Get the time the file was created. Returns null if unknown.
     *
     * @since 1.3
     */
    public Date getCreateTime() throws IOException;
}
