/*
 * Licensed Materials - Property of IBM
 *
 * (C) Copyright IBM Corp. 2007, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.util;

/**
 * Sleep for the specified time (in millseconds).
 */
public class Sleep {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class methods
	 */

	/**
	 * Sleep for the specified time (in millseconds).
	 * 
	 * @param cla
	 *            the command line arguments; argument 1 is the time to sleep in milliseconds.
	 */
	public static void main(String[] cla) {

		if (cla.length != 1) {

			System.err.println("Usage: Sleep <millis>");
			System.exit(0);

		}

		try {
			Thread.sleep(Long.parseLong(cla[0]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
