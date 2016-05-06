/*
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
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FabricFormatter extends Formatter {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /*
     * Class fields
     */

    private String title = "unknown";
    private int maxThreadNameLength = 0;
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

    private StringBuilder pad(String string, int padTo) {
        StringBuilder buf = new StringBuilder();
        for (int p = padTo - string.length(); p > 0; p--) {
            buf.append(' ');
        }
        return buf;
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

        /* Add the thread ID */

        String threadName = Thread.currentThread().getName();
        if (threadName != null) {
            maxThreadNameLength = (threadName.length() > maxThreadNameLength ? threadName.length()
                    : maxThreadNameLength);
        }

        logMessage.append('[').append(threadName).append(pad(threadName, maxThreadNameLength)).append(']').append(' ');

        /* Add the name of the log level */
        logMessage.append('[').append(r.getLevel().getName()).append(pad(r.getLevel().getName(), 7)).append(']')
                .append(' ');

        if (longMessages) {

            /* Add the name of the logger */
            logMessage.append('[').append(r.getLoggerName()).append("] ");

            /* Add the short name of the source class */
            String className = r.getSourceClassName();
            className = className.substring(className.lastIndexOf('.') + 1, className.length());
            logMessage.append('[').append(className).append('.').append(r.getSourceMethodName()).append("] ");

        }

        /* Add the body of the message */

        logMessage.append(": ");
        char startc = r.getMessage().charAt(0);

        if (startc == '-' || startc == '<') {
            /* Nothing to add */
        } else if (r.getLevel().intValue() <= Level.FINE.intValue()) {
            logMessage.append(FLog.indent());
        }

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
