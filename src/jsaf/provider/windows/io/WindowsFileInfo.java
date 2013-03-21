// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.io;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import jsaf.io.fs.DefaultMetadata;
import jsaf.intf.windows.io.IWindowsFilesystem;
import jsaf.intf.windows.io.IWindowsFileInfo;

/**
 * Implements extended attributes of a file on Windows.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class WindowsFileInfo extends DefaultMetadata implements IWindowsFileInfo {
    private int winType;
    private Map<String, String> peHeaders;

    public WindowsFileInfo(Type type, String path, String canonicalPath, Date ctime, Date mtime, Date atime, long length,	
		int winType, Map<String, String> peHeaders) {

	super(type, path, null, canonicalPath, ctime, mtime, atime, length);
	this.winType = winType;
	this.peHeaders = peHeaders;
    }

    // Implement IWindowsFileInfo

    /**
     * Returns one of the FILE_TYPE_ constants.
     */
    public int getWindowsFileType() throws IOException {
	return winType;
    }

    public Map<String, String> getPEHeaders() throws IOException {
	return peHeaders;
    }
}
