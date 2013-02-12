// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.io;

import java.io.IOException;

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
}
