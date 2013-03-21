// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.io;

import java.io.IOException;
import java.util.Map;

import jsaf.intf.io.IFileEx;

/**
 * Defines extended attributes of a file on Windows.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IWindowsFileInfo extends IFileEx {
    /**
     * Either the type of the specified file is unknown, or the function failed.
     *
     * @since 1.0
     */
    int FILE_TYPE_UNKNOWN = 0x0000;

    /**
     * The specified file is a disk file.
     *
     * @since 1.0
     */
    int FILE_TYPE_DISK = 0x0001;

    /**
     * The specified file is a character file, typically an LPT device or a console.
     *
     * @since 1.0
     */
    int FILE_TYPE_CHAR = 0x0002;

    /**
     * The specified file is a socket, a named pipe, or an anonymous pipe.
     *
     * @since 1.0
     */
    int FILE_TYPE_PIPE = 0x0003;

    /**
     * Unused.
     *
     * @since 1.0
     */
    int FILE_TYPE_REMOTE = 0x8000;

    /**
     * The handle that identifies a directory.
     *
     * @since 1.0
     */
    int FILE_ATTRIBUTE_DIRECTORY = 0x10;

    /**
     * Returns one of the FILE_TYPE_ constants.
     *
     * @since 1.0
     */
    int getWindowsFileType() throws IOException;

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
     * Retrieve information from the Portable Execution (PE) format file headers.
     *
     * @return null if not a PE-format file
     * @since 1.0.1
     */
    Map<String, String> getPEHeaders() throws IOException;
}
