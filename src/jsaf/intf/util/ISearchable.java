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
     * Condition type indicating a search for something that "equals" the value.
     *
     * @since 1.0
     */
    int TYPE_EQUALITY	= 0;

    /**
     * Condition type indicating a search for something that is "not equal" to the value.
     *
     * @since 1.0
     */
    int TYPE_INEQUALITY	= 1;

    /**
     * Condition type indicating a search for something that matches a regex pattern specified by the value (which should
     * be a java.util.regex.Pattern instance).
     *
     * @since 1.0
     */
    int TYPE_PATTERN	= 2;

    /**
     * Depth condition field ID for recursive searches.
     *
     * @since 1.0
     */
    int FIELD_DEPTH	= 0;

    /**
     * Starting point condition field ID for recursive searches.
     *
     * @since 1.0
     */
    int FIELD_FROM	= 1;

    /**
     * Unlimited depth condition value for recursive searches.
     *
     * @since 1.0
     */
    int DEPTH_UNLIMITED = -1;

    /**
     * DEPTH_UNLIMITED wrapped in an Integer object, for use as a condition value.
     *
     * @since 1.0
     */
    Object DEPTH_UNLIMITED_VALUE = new Integer(DEPTH_UNLIMITED);

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
     * @since 1.0
     */
    Collection<T> search(List<ICondition> conditions) throws Exception;

    /**
     * A search condition interface.
     *
     * @since 1.0
     */
    public interface ICondition {
	/**
	 * The type of assertion made by the condition, e.g., TYPE_EQUALITY, TYPE_INEQUALITY, TYPE_PATTERN.
	 *
	 * @since 1.0
	 */
	int getType();

	/**
	 * The scope of assertion made by the condition, i.e., a field, indicated by an arbitrary integer.
	 *
	 * @since 1.0
	 */
	int getField();

	/**
	 * The value of assertion made by the condition. The type can vary depending on the field and the condition type.
	 *
	 * @since 1.0
	 */
	Object getValue();
    }

    /**
     * A condition for unlimited recursion.
     *
     * @since 1.0
     */
    ICondition RECURSE = new GenericCondition(FIELD_DEPTH, TYPE_EQUALITY, DEPTH_UNLIMITED_VALUE);

    /**
     * Implement as: return new GenericCondition(field, type, value)
     *
     * @since 1.0
     */
    ISearchable.ICondition condition(int field, int type, Object value);

    // Class definitions for implementors

    /**
     * A class definition representing a generic condition (implementing ICondition) that can be used as a convenience
     * class by implementors.
     *
     * @since 1.0
     */
    static class GenericCondition implements ISearchable.ICondition {
	private int field, type;
	private Object value;

	public GenericCondition(int field, int type, Object value) {
	    this.field = field;
	    this.type = type;
	    this.value = value;
	}

	// Implement ICondition

	public int getType() {
	    return type;
	}

	public int getField() {
	    return field;
	}

	public Object getValue() {
	    return value;
	}
    }
}
