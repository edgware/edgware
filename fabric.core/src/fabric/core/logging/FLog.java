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
     * Answers the current log message indent level for the calling thread.
     *
     * @return the level.
     */
    protected static int indentLevel() {

        int indentLevel = 0;

        synchronized (nesting) {
            Integer indent = nesting.get(Thread.currentThread().getName());
            indentLevel = (indent == null) ? 0 : indent;
        }

        return indentLevel;
    }

    /**
     * Answers a string indent matching the log message indent level for the calling thread.
     *
     * @return the indent.
     */
    protected static String indent() {

        StringBuilder indent = new StringBuilder();
        int indentLevel = indentLevel();

        for (int i = 0; i <= indentLevel; i++) {
            indent.append('-');
        }

        indent.append(' ');

        return indent.toString();
    }

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

    /**
     * Answers a single string containing the string representations of an array of objects.
     * <p>
     * The string is of the form:
     *
     * <pre>
     * [<em>string-1</em>][<em>string-2</em>][<em>string-3</em>]...
     * </pre>
     *
     * </p>
     *
     * @param array
     * @return
     */
    public static String arrayAsString(Object[] array) {

        StringBuilder s = new StringBuilder();

        if (array != null) {

            for (Object a : array) {
                s.append('[').append(a).append(']');
            }
        }
        return s.toString();
    }

    /*
     * Aliases for standard logging methods.
     */

    public static void finest(Logger l, String msg, Object... args) {
        l.log(Level.FINEST, msg, args);
    }

    public static void finest(Logger l, String msg) {
        l.log(Level.FINEST, msg);
    }

    public static void finer(Logger l, String msg, Object... args) {
        l.log(Level.FINER, msg, args);
    }

    public static void finer(Logger l, String msg) {
        l.log(Level.FINER, msg);
    }

    public static void fine(Logger l, String msg, Object... args) {
        l.log(Level.FINE, msg, args);
    }

    public static void fine(Logger l, String msg) {
        l.log(Level.FINE, msg);
    }

    public static void info(Logger l, String msg, Object... args) {
        l.log(Level.INFO, msg, args);
    }

    public static void info(Logger l, String msg) {
        l.log(Level.INFO, msg);
    }

    public static void warn(Logger l, String msg, Object... args) {
        l.log(Level.WARNING, msg, args);
    }

    public static void warn(Logger l, String msg) {
        l.log(Level.WARNING, msg);
    }

    public static void severe(Logger l, String msg, Object... args) {
        l.log(Level.SEVERE, msg, args);
    }

    public static void severe(Logger l, String msg) {
        l.log(Level.SEVERE, msg);
    }
}