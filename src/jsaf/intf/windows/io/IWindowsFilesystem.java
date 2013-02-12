// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.io;

import jsaf.intf.io.IFilesystem;

/**
 * Interface extensions for an IFilesystem on Windows.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IWindowsFilesystem extends IFilesystem {
    /**
     * String delimiter for Windows filesystem paths.
     *
     * @since 1.0
     */
    String DELIM_STR = "\\";

    /**
     * Character delimiter for Windows filesystem paths.
     *
     * @since 1.0
     */
    char DELIM_CH = '\\';

    /**
     * An enumeration of types, corresponding to the low-level DRIVE_ IDs.
     *
     * @since 1.0
     */
    enum FsType {
	/**
	 * FsType indicating a filesystem whose type is not known.
	 *
	 * @since 1.0
	 */
	UNKNOWN("unknown", DRIVE_UNKNOWN),

	/**
	 * FsType for removable media (e.g., a floppy drive).
	 *
	 * @since 1.0
	 */
	REMOVABLE("removable", DRIVE_REMOVABLE),

	/**
	 * FsType for fixed media (e.g., a hard disk drive).
	 *
	 * @since 1.0
	 */
	FIXED("fixed", DRIVE_FIXED),

	/**
	 * FsType for netowrk-mounted media (e.g., a drive-mapped share).
	 *
	 * @since 1.0
	 */
	REMOTE("remote", DRIVE_REMOTE),

	/**
	 * FsType for a CD-ROM or DVD-ROM drive.
	 *
	 * @since 1.0
	 */
	CDROM("cdrom", DRIVE_CDROM),

	/**
	 * FsType for a RAM disk.
	 *
	 * @since 1.0
	 */
	RAMDISK("ramdisk", DRIVE_RAMDISK);

	private String value;
	private int id;

	private FsType(String value, int id) {
	    this.value = value;
	    this.id = id;
	}

	/**
	 * Get the String value for the FsType.
	 *
	 * @since 1.0
	 */
	public String value() {
	    return value;
	}

	/**
	 * Get the ID for the FsType.
	 *
	 * @since 1.0
	 */
	public int id() {
	    return id;
	}

	/**
	 * Return the FsType corresponding to the specified String value.
	 *
	 * @since 1.0
	 */
	public static FsType typeOf(String value) {
	    for (FsType fs : values()) {
		if (fs.value().equals(value)) {
		    return fs;
		}
	    }
	    return UNKNOWN;
	}

	/**
	 * Return the FsType corresponding to the specified ID.
	 *
	 * @since 1.0
	 */
	public static FsType typeOf(int id) {
	    for (FsType fs : values()) {
		if (fs.id() == id) {
		    return fs;
		}
	    }
	    return UNKNOWN;
	}
    }

    /**
     * ID indicating the drive type cannot be determined.
     *
     * @since 1.0
     */
    int DRIVE_UNKNOWN = 0;

    /**
     * ID indicating the root path is invalid; for example, there is no volume mounted at the specified path.
     *
     * @since 1.0
     */
    int DRIVE_NO_ROOT_DIR = 1;

    /**
     * ID indicating the drive has removable media; for example, a floppy drive, thumb drive, or flash card reader.
     *
     * @since 1.0
     */
    int DRIVE_REMOVABLE = 2;

    /**
     * ID indicating the drive has fixed media; for example, a hard disk drive or flash drive.
     *
     * @since 1.0
     */
    int DRIVE_FIXED = 3;

    /**
     * ID indicating the drive is a remote (network) drive.
     *
     * @since 1.0
     */
    int DRIVE_REMOTE = 4;

    /**
     * ID indicating the drive is a CD-ROM drive.
     *
     * @since 1.0
     */
    int DRIVE_CDROM = 5;

    /**
     * ID indicating the drive is a RAM disk.
     *
     * @since 1.0
     */
    int DRIVE_RAMDISK = 6;
}
