// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.system;

import java.util.Iterator;

/**
 * A representation of a system environment.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IEnvironment extends Iterable<String> {
    /**
     * Get an environment variable by name.
     *
     * @since 1.0
     */
    public String getenv(String var);

    /**
     * If s contains references to variables defined in this environment (i.e., "%NAME%"), then the returned string replaces
     * them with their values from the environment.
     *
     * For example, if there is an environment variable called FOO with a value of "bar", the string "My %FOO%" passed into
     * this method would return the string "My bar".
     *
     * @since 1.0
     */
    public String expand(String s);

    /**
     * Returns an Iterator over the names of the variables defined in this environment.
     *
     * @since 1.0
     */
    public Iterator<String> iterator();

    /**
     * Returns a String array suitable for passing into ISession.createProcess as the environment argument.
     *
     * @see jsaf.intf.system.ISession.createProcess
     *
     * @since 1.0
     */
    public String[] toArray();
}

