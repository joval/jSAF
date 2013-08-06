// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.unix.io;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import jsaf.intf.io.IFilesystem;
import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;

/**
 * An interface describing the platform-specific requirements for a UnixFilesystem driver.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUnixFilesystemDriver extends ILoggable {
    /**
     * Shortcut for getMounts(typeFilter, false)
     *
     * @since 1.0
     */
    public Collection<IFilesystem.IMount> getMounts(Pattern typeFilter) throws Exception;

    /**
     * Get a list of mount points.
     *
     * @param typeFilter A regex pattern indicating the /types/ of filesystems to include or exclude from the result. Use null
     *                   for an unfiltered list of mount points.
     * @param include    Use true for an include filter, false for an exclude filter
     *
     *
     * @since 1.0.1
     */
    public Collection<IFilesystem.IMount> getMounts(Pattern typeFilter, boolean include) throws Exception;

    /**
     * Returns a string containing the correct find command for the Unix flavor.
     *
     * The resulting command will follow links, but restrict results to the originating filesystem.
     *
     * @since 1.0
     */
    public String getFindCommand(List<ISearchable.ICondition> conditions);

    /**
     * Returns a command whose output can be fed into the nextFileInfo method, to return file information for the path.
     *
     * @param path an unquoted path string (it will be quoted within the resulting command)
     *
     * @since 1.0.1
     */
    public String getStatCommand(String path);

    /**
     * Generate a UnixFileInfo based on the output from the Stat command.  The lines iterator may contain output
     * representing one or more stat commands, but this method is expected to retrieve only the very next FileInfo.
     * If there are no more lines, this method should return null.
     *
     * @since 1.0
     */
    public IUnixFileInfo nextFileInfo(Iterator<String> lines);
}
