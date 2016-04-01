/*
 * (C) Copyright IBM Corp. 2014, 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generate a random ID.
 */
public class FLog {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014, 2016";

    private static final int PARAM_MAX = 25;

    private static HashMap<String, Integer> nesting = new HashMap<String, Integer>();

    /*
     * Class methods
     */

    /**
     * Logs entry to a method.
     *
     * @param logger
     *            the logger to use.
     *
     * @param level
     *            the logging level.
     *
     * @param caller
     *            the calling class.
     *
     * @param method
     *            the name of the method.
     *
     * @param params
     *            the method's arguments, or <code>null</code> if there are none.
     */
    public static void enter(Logger logger, Level level, Object caller, String method, Object... params) {

        try {
            if (logger != null && level != null && logger.isLoggable(level)) {

                StringBuilder buf = new StringBuilder();
                String threadID = Thread.currentThread().getName();
                Integer indent = null;

                synchronized (nesting) {
                    indent = nesting.get(threadID);
                    indent = (indent == null) ? 1 : indent + 1;
                    nesting.put(threadID, indent);
                }

                for (int i = 0; i < indent; i++) {
                    buf.append('-');
                }

                buf.append('>');
                buf.append(' ');

                if (caller != null) {
                    buf.append(caller.getClass().getName());
                    buf.append(':');
                }

                buf.append(method);
                buf.append('(');
                for (int p = 0; params != null && p < params.length; p++) {
                    StringBuilder nextParam = new StringBuilder((params[p] != null) ? params[p].toString() : "null");
                    buf.append(trim(nextParam, PARAM_MAX));
                    if (p < params.length - 1) {
                        buf.append(", ");
                    }
                }
                buf.append(')');

                logger.log(level, buf.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs exit from a method.
     *
     * @param logger
     *            the logger to use.
     *
     * @param level
     *            the logging level.
     *
     * @param caller
     *            the calling class.
     *
     * @param method
     *            the name of the method.
     *
     * @param retVal
     *            the return value of the method, or <code>null</code> if none.
     */
    public static void exit(Logger logger, Level level, Object caller, String method, Object retVal) {

        try {
            if (logger != null && level != null && logger.isLoggable(level)) {

                StringBuilder buf = new StringBuilder();
                String threadID = Thread.currentThread().getName();
                Integer indent = null;

                synchronized (nesting) {
                    indent = nesting.get(threadID);
                    indent = (indent == null || indent == 0) ? 0 : indent - 1;
                    nesting.put(threadID, indent);
                }

                buf.append('<');

                for (int i = 0; i <= indent; i++) {
                    buf.append('-');
                }

                buf.append(' ');

                if (caller != null) {
                    buf.append(caller.getClass().getName());
                    buf.append(':');
                }

                buf.append(method);
                buf.append('(');
                StringBuilder ret = new StringBuilder((retVal != null) ? retVal.toString() : "");
                buf.append(trim(ret, PARAM_MAX));
                buf.append(')');

                logger.log(level, buf.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shortens a string to the specified precision; ellipses will be inserted into the middle of the string in place of
     * the characters removed.
     *
     * @param string
     *            the string to shorten.
     *
     * @param max
     *            the maximum length of the string.
     */
    public static StringBuilder trim(StringBuilder string, int max) {

        StringBuilder shortForm = new StringBuilder(string);

        if (string != null && string.length() > max) {

            int len = max - 3;
            int start = len / 2;
            int end = string.length() - (max - start - 3);
            shortForm.replace(start, end, "...");

        }

        return shortForm;
    }

    /**
     * Shortens a string to the default precision; ellipses will be inserted into the middle of the string in place of
     * the characters removed.
     *
     * @param string
     *            the string to shorten.
     */
    public static StringBuilder trim(StringBuilder string) {

        return trim(string, PARAM_MAX);
    }

    /**
     * Shortens a string to the default precision; ellipses will be inserted into the middle of the string in place of
     * the characters removed.
     *
     * @param string
     *            the string to shorten.
     */
    public static String trim(String string) {

        return trim(new StringBuilder(string), PARAM_MAX).toString();
    }

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
