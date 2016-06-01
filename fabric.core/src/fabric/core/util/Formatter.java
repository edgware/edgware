/*
 * (C) Copyright IBM Corp. 2007
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.util;

/**
 * Formats a printf-style format string.
 * <p>
 * This class is used when the standard Java formatting class is not available.
 * </p>
 */
public class Formatter {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

    /*
     * Class fields
     */

    private StringBuilder formattedString = null;

    /*
     * Class methods
     */

    /**
     * Formats a simple printf-style string.
     * <p>
     * This method is provided primarily for Java platforms (for example some configurations of J2ME) that do not
     * support the standard Java string formatting capability. Note that this is not a complete implementation of
     * printf-style format specifiers; the only supported formats are:
     * <ul>
     * <li><code>%d</code>: integer insert</li>
     * <li><code>%f</code>: floating point insert</li>
     * <li><code>%s</code>: string insert</li>
     * <li><code>%c</code>: character insert</li>
     * </ul>
     * Furthermore, no field width specifiers are supported. The '<code>%</code>' character can be escaped with either a
     * '<code>\</code>' or a second '<code>%</code>'.
     * </p>
     *
     * @param format
     *            the printf-style format string.
     *
     * @param inserts
     *            the string inserts.
     *
     * @return the formatted string.
     */
    public String format(String format, Object... inserts) {

        formattedString = new StringBuilder();

        /* Extract the characters of the format string */
        char[] formatChars = format.toCharArray();

        /* To track the current insert */
        int currentInsert = 0;

        /* For each character in the format string... */
        for (int c = 0; c < formatChars.length; c++) {

            switch (formatChars[c]) {

                case '%':

                    /* This should be the start of a format specifier */

                    /* If there is a format specifier... */
                    if (++c < formatChars.length) {

                        Object nextInsert = null;
                        if (inserts.length > currentInsert) {
                            nextInsert = inserts[currentInsert++];
                        } else {
                            break; // illegal character
                        }

                        /* Decode and action it */
                        switch (formatChars[c]) {

                            case 'd':

                                /* Integer */
                                nextInsert = (nextInsert != null) ? nextInsert : "null";
                                formattedString.append(nextInsert.toString());
                                break;

                            case 'f':

                                /* Floating point */
                                nextInsert = (nextInsert != null) ? nextInsert : "null";
                                formattedString.append(nextInsert.toString());
                                break;

                            case 's':

                                /* String */
                                nextInsert = (nextInsert != null) ? nextInsert : "null";
                                formattedString.append(nextInsert.toString());
                                break;

                            case 'c':

                                /* Character */
                                nextInsert = (nextInsert != null) ? nextInsert : "null";
                                formattedString.append(nextInsert.toString());
                                break;

                            case '%':

                                /* Literal '%' */
                                formattedString.append(formatChars[c]);
                                currentInsert--;
                                break;

                            default:

                                String error = "Unrecognised format specifier: " + formatChars[c];
                                System.err.println(error);
                                throw new RuntimeException(error);

                        }

                    } else {

                        String error = "Incomplete format specifier in: " + format;
                        System.err.println(error);
                        throw new RuntimeException(error);

                    }

                    break;

                case '\\':

                    /* Ignore escaped character */
                    c++;

                default:

                    formattedString.append(formatChars[c]);
                    break;

            }
        }

        return formattedString.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return (formattedString != null) ? formattedString.toString() : "" + null;

    }
}
