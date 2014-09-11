/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Generate a random ID.
 */
public class LogUtil {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

	/**
	 * Answers a string containing a throwable's stack trace.
	 * 
	 * @param throwable
	 *            the throwable.
	 * 
	 * @return the string.
	 */
	public static String stackTrace(Throwable throwable) {

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		return stringWriter.toString();

	}
}
