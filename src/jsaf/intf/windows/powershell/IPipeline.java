// Copyright (C) 2020-2021, JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.powershell;

import java.util.Collection;

/**
 * An interface for creating a large set of String objects that will be pipelined into a Powershell expression.
 *
 * For example, to use an IPipeline to express <code>"a", "b", "c" | %{"arg: {0}" -f $_}</code>, you would:
 * <pre>
 * IPipeline&lt;String&gt; p;
 * ...
 * p.add("a");
 * p.add("b");
 * p.add("c");
 * p.setExpression("%{\"arg: {0}\" -f $_}");
 * </pre>
 *
 * @since 1.5.1
 * @version %I% %G%
 */
public interface IPipeline<T> extends Iterable<T> {
    int size();

    /**
     * Add an argument to the expression.
     */
    void add(T arg);

    /**
     * Add multiple arguments to the expression.
     *
     * @since 1.6.10
     */
    void addAll(Collection<T> arg);

    void setExpression(String expression);

    String getExpression();

    /**
     * Get a unique checksum representation of the pipeline. Useful for result caching.
     *
     * @since 1.6.8
     */
    String checksum();
}
