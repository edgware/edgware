/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.traceroute;

import java.util.Comparator;

public class TraceRecordComparator implements Comparator<TraceRecord> {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	@Override
	public int compare(TraceRecord arg0, TraceRecord arg1) {

		return arg0.timeStamp().compareTo(arg1.timeStamp());
	}

}
