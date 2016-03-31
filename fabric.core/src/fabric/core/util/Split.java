/*
 * (C) Copyright IBM Corp. 2006, 2007
 *  
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.util;

import java.util.ArrayList;

/**
 * Splits a string around matches of the specified substring.
 * <p>
 * This class is used when the standard Java <code>String.split()</code> method is not available.
 * </p>
 */
public class Split {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2007";

	/*
	 * Class methods
	 */

	/**
	 * Split the specified string.
	 * 
	 * @param source
	 *            the source string to split.
	 * 
	 * @param pattern
	 *            the point at which to split the string.
	 * 
	 * @param maxParts
	 *            the maximum number of parts to split the string into.
	 * 
	 * @return the substrings.
	 */
	public static String[] divide(String source, String pattern, int maxParts) {

		ArrayList<String> partsList = new ArrayList<String>();

		/* For each part... */
		for (int partCount = 1; partCount < maxParts && source.indexOf(pattern) != -1; partCount++) {

			/* Get the next part */
			partsList.add(source.substring(0, source.indexOf(pattern)));

			/* Remove it from the source string ready to find the next */
			source = source.substring(source.indexOf(pattern) + pattern.length(), source.length());
		}

		/* If there is a remaining part... */
		if (source.length() > 0) {
			/* Record it */
			partsList.add(source);
		}

		/* Generate the result array */
		String[] parts = partsList.toArray(new String[partsList.size()]);

		return parts;
	}

	/**
	 * 
	 * @param source
	 *            the source string to split.
	 * 
	 * @param pattern
	 *            the point at which to split the string.
	 * 
	 * @return the substrings.
	 */
	public static String[] divide(String source, String pattern) {

		return divide(source, pattern, source.length());

	}
}
