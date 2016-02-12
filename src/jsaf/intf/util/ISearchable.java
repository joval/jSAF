// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import jsaf.Message;

/**
 * An interface for searching something.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface ISearchable<T> {
    int UNLIMITED = 0;

    /**
     * Sets the maximum number of results that will be returned for any single List of search conditions. Any additional
     * matches will be silently truncated.
     *
     * @param limit The desired limit. The default (if this method is never called) is 0, meaning there should be no limit.
     *
     * @since 1.3
     */
    void setResultLimit(int limit);

    /**
     * Recursively search for elements matching the given pattern and return an IResult.
     *
     * @param conditions a list of search conditions
     *
     * @throws IllegalArgumentException if there is a problem with one or more search conditions
     *
     * @since 1.2
     */
    IResult<T> search(List<Condition> conditions) throws IllegalArgumentException;

    /**
     * Perform multiple searches in parallel.
     *
     * @param conditionLists a List of Lists of search conditions
     *
     * @return a List of results, whose order corresponds to the conditionLists
     *
     * @throws IllegalArgumentException if there is a problem with one or more search conditions in any list
     *
     * @since 1.2
     */
    List<IResult<T>> searches(List<List<Condition>> conditionLists) throws IllegalArgumentException;

    /**
     * An interface for search results.
     *
     * @since 1.2
     */
    public interface IResult<T> {
	/**
	 * Get the result items.
	 */
	Collection<T> get();

	/**
	 * Determine whether there were errors which could have truncated the search results, such as a timeout.
         *
         * @since 1.3
	 */
	boolean hasErrors();

	/**
	 * Get the errors (if any);
         *
         * @since 1.3
	 */
	Collection<String> getErrors();
    }

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

    /**
     * An Exception class for field IDs that are not supported by a particular ISearchable implementation.
     *
     * @since 1.3.3
     */
    public class UnsupportedSearchFieldException extends IllegalArgumentException {
	public UnsupportedSearchFieldException(Condition condition) {
	    super(Message.getMessage(Message.ERROR_SEARCH_FIELD, condition.toString()));
	}
    }

    /**
     * An Exception class for ISearchable.Conditions that are not supported by a particular ISearchable implementation.
     *
     * @since 1.3.3
     */
    public class UnsupportedSearchConditionException extends IllegalArgumentException {
	public UnsupportedSearchConditionException(Condition condition) {
	    super(Message.getMessage(Message.ERROR_SEARCH_CONDITION, condition.toString()));
	}
    }
}
