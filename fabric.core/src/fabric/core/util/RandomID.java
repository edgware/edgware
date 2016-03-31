/*
 * (C) Copyright IBM Corp. 2007
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.util;

/**
 * Generate a random ID.
 */
public class RandomID {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

    /*
     * Class methods
     */

    /**
     * Generate a random ID.
     * 
     * @param maxLength
     *            the maximum length of the ID.
     * 
     * @return the ID.
     */
    public static String generate(int maxLength) {

        long multiplier = (long) Math.pow(10, maxLength);
        long random = (long) (Math.random() * multiplier);
        return Long.toHexString(random);

    }

    /**
     * Generate a random ID with a prefix.
     * 
     * @param prefix
     *            the caller-supplied prefix for the ID.
     * 
     * @param maxLength
     *            the maximum length of the ID.
     * 
     * @return the random ID, prefixed with the caller supplied prefix.
     */
    public static String generate(String prefix, int maxLength) {

        /* If the caller has not supplied a prefix... */
        if (prefix == null) {
            prefix = "";
        }
        /* Else if the supplied prefix is too long... */
        else if (prefix.length() >= maxLength) {
            throw new IllegalArgumentException("Prefix [" + prefix + "] too long");
        }

        /* Calculate how much space is available */
        int remainingLength = maxLength - prefix.length();

        /* Generate the result */
        String randomID = prefix + generate(remainingLength);

        return randomID;
    }
}
