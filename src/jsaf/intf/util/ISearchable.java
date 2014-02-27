// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An interface for searching something.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISearchable<T> {
    /**
     * Hazard a guess for the parent path of the specified pattern. Returns null if indeterminate. This method is
     * useful when building "from" conditions.
     *
     * @since 1.0
     */
    String[] guessParent(Pattern p, Object... args);

    /**
     * Recursively search for elements matching the given pattern.
     *
     * @param conditions a list of search conditions.
     *
     * @since 1.2
     */
    Collection<T> search(List<Condition> conditions) throws Exception;

    /**
     * A condition for unlimited recursion.
     *
     * @since 1.2
     */
    Condition RECURSE = new Condition(Condition.FIELD_DEPTH, Condition.TYPE_EQUALITY, Condition.DEPTH_UNLIMITED_VALUE);

    /**
     * Base class for search conditions. Implementations defining new fields should extend this class, and define the
     * fields in the extension.
     *
     * @since 1.2
     */
    public class Condition {
	/**
	 * Condition type indicating a search for something that "equals" the value.
	 *
	 * @since 1.2
	 */
	public static final int TYPE_EQUALITY = 0;

	/**
	 * Condition type indicating a search for something that is "not equal" to the value.
	 *
	 * @since 1.2
	 */
	public static final int TYPE_INEQUALITY = 1;

	/**
	 * Condition type indicating a search for something that matches a regex pattern specified by the value (which
	 * should be a java.util.regex.Pattern instance).
	 *
	 * @since 1.2
	 */
	public static final int TYPE_PATTERN = 2;

	/**
	 * Condition type used with array and collection values, indicating a search for something matching any
	 * (i.e., at least one) of the values.
	 *
	 * @since 1.2
	 */
	public static final int TYPE_ANY = 3;

	/**
	 * Condition type used with array and collection values, indicating a search for something NOT matching any
	 * (i.e., at least one) of the values.
	 *
	 * @since 1.2
	 */
	public static final int TYPE_NONE = 4;

	/**
	 * Depth condition field ID for recursive searches.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_DEPTH = 0;

	/**
	 * Starting point condition field ID for recursive searches.
	 *
	 * @since 1.2
	 */
	public static final int FIELD_FROM = 1;

	/**
	 * Unlimited depth condition value for recursive searches.
	 *
	 * @since 1.2
	 */
	public static final int DEPTH_UNLIMITED = -1;

	/**
	 * DEPTH_UNLIMITED wrapped in an Integer object, for use as a condition value.
	 *
	 * @since 1.2
	 */
	public static final Object DEPTH_UNLIMITED_VALUE = new Integer(DEPTH_UNLIMITED);

	private int field, type;
	private Object value;

	protected Condition(int field, int type, Object value) {
	    this.field = field;
	    this.type = type;
	    this.value = value;
	}

	/**
	 * Get the type of assertion made by the condition, e.g., TYPE_EQUALITY, TYPE_INEQUALITY, TYPE_PATTERN.
	 *
	 * @since 1.2
	 */
	public final int getType() {
	    return type;
	}

	/**
	 * Get the scope of assertion made by the condition, i.e., a field, indicated by an arbitrary integer.
	 *
	 * @since 1.2
	 */
	public final int getField() {
	    return field;
	}

	/**
	 * Get the value of assertion made by the condition. The type can vary depending on the field and the condition
	 * type.
	 *
	 * @since 1.2
	 */
	public final Object getValue() {
	    return value;
	}

	@Override
	public final String toString() {
	    return "Condition: " + getClass().getName() + " type=" + type + ", field=" + field + ", value=" + value.toString();
	}
    }
}
