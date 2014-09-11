/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FabricFormatter extends Formatter {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class fields
	 */

	private String title = "unknown";

	private boolean longMessages = false;

	/*
	 * Class methods
	 */

	public FabricFormatter() {

		super();
	}

	public FabricFormatter(boolean longMessages) {

		super();
		this.longMessages = longMessages;

	}

	public void setTitle(String title) {

		this.title = title;
	}

	@Override
	public String format(LogRecord r) {

		StringBuilder logMessage = new StringBuilder();

		/* Add a timestamp */
		Date d = new Date(r.getMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS ");
		String date = sdf.format(new Date(r.getMillis()));
		logMessage.append(date);

		/* Add the title of the logging component */
		logMessage.append('[').append(this.title).append("] ");

		/* Add the name of the log level */
		logMessage.append('[' + r.getLevel().getName());
		for (int i = 7 - r.getLevel().getName().length(); i > 0; i--) {
			logMessage.append(' ');
		}
		logMessage.append("] ");

		if (longMessages) {

			/* Add the name of the logger */
			logMessage.append('[').append(r.getLoggerName()).append("] ");

			/* Add the short name of the source class */
			String className = r.getSourceClassName();
			className = className.substring(className.lastIndexOf('.')+1, className.length());;
			logMessage.append('[').append(className).append(" ");
			logMessage.append(r.getSourceMethodName());
			logMessage.append("] ");
			
			
		}

		/* Add the body of the message */
		logMessage.append(": ");
		logMessage.append(formatMessage(r));

		/* If an exception is involved... */
		if (r.getThrown() != null) {

			logMessage.append("Throwable: ");
			Throwable t = r.getThrown();
			PrintWriter pw = null;

			try {

				StringWriter sw = new StringWriter();
				pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				logMessage.append(sw.toString());

			} finally {

				if (pw != null) {
					try {
						pw.close();
					} catch (Exception e) {
					}
				}
			}
		}

		return logMessage.toString() + "\n";
	}
}
