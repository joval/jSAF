// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.io;

import java.io.IOException;
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
     * Returns the file's WindowsType.
     *
     * @since 1.3
     */
    WindowsType getWindowsType() throws IOException;

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
